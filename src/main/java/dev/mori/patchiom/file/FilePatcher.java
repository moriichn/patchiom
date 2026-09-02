package dev.mori.patchiom.file;

import dev.mori.patchiom.log.OutputAdapter;
import dev.mori.patchiom.util.UnicodeNormalize;
import org.objectweb.asm.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class FilePatcher {

	// subject to change, I'm ready for war, patch this shit, and I'll update it.
	private static final Set<String> CANDIDATE_CLASS_NAMES = Set.of("com/moulberry/axiom/utils/Authorization.class");

	public static void patchJar(Path inputJar, Path outputJar, OutputAdapter log) throws IOException {
		try (
				InputStream in = Files.newInputStream(inputJar);
				JarInputStream jarInput = new JarInputStream(in);
				OutputStream out = Files.newOutputStream(outputJar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
		) {
			if(!patchJar(jarInput, out, log)) {
				throw new IllegalStateException("Can't find anything to patch. If this is a version of the mod newer than this release of the patcher, please open an issue.");
			}
		}
	}

	private static boolean patchJar(JarInputStream input, OutputStream output, OutputAdapter log) throws IOException {
		try (JarOutputStream jarOutput = new JarOutputStream(output)) {
			boolean patched = false;
			JarEntry entry;

			while ((entry = input.getNextJarEntry()) != null) {
				String name = entry.getName();
				JarEntry outputEntry = new JarEntry(name);
				outputEntry.setTime(entry.getTime());

				if (entry.getComment() != null) {
					outputEntry.setComment(entry.getComment());
				}

				if (entry.getExtra() != null) {
					outputEntry.setExtra(entry.getExtra());
				}

				jarOutput.putNextEntry(outputEntry);

				byte[] data = input.readAllBytes();

				if (CANDIDATE_CLASS_NAMES.contains(UnicodeNormalize.normalizeCharacters(name))) {
					log.info("Patching class " + name);
					String plain = name.substring(0, name.lastIndexOf(".class"));
					data = patchClass(data, plain, plain + "$ServerAuthorization");
					patched = true;
				} else if (name.endsWith(".jar")) {
					log.progress("Discovered nested jar '" + name + "'..");

					try (JarInputStream nestedInput = new JarInputStream(new ByteArrayInputStream(data))) {
						ByteArrayOutputStream nestedOutput = new ByteArrayOutputStream();
						patched |= patchJar(nestedInput, nestedOutput, log);
						data = nestedOutput.toByteArray();
					}
				}

				jarOutput.write(data);
				jarOutput.closeEntry();
			}

			return patched;
		}
	}

	private static byte[] patchClass(byte[] classBytes, String owner, String resultEnumDescriptor) {
		ClassReader reader = new ClassReader(classBytes);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

		ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				Type sType = Type.getReturnType(descriptor);
				if (name.equals("checkServer") && sType.equals(Type.getType(CompletableFuture.class))) {

					MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
					mv.visitCode();

					setBooleanFieldTrue(mv, owner, "hasServerCommercialLicense");

					mv.visitFieldInsn(
							Opcodes.GETSTATIC,
							resultEnumDescriptor,
							"COMMERCIAL",
							"L" + resultEnumDescriptor + ";"
					);

					mv.visitMethodInsn(
							Opcodes.INVOKESTATIC,
							"java/util/concurrent/CompletableFuture",
							"completedFuture",
							"(Ljava/lang/Object;)Ljava/util/concurrent/CompletableFuture;",
							false
					);

					mv.visitInsn(Opcodes.ARETURN);
					mv.visitMaxs(0, 0);
					mv.visitEnd();
					return null;
				}

				Type cmType = Type.getReturnType(descriptor);
				if (name.equals("checkCommercial") && cmType.equals(Type.getType(CompletableFuture.class))) {
					MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
					mv.visitCode();

					setBooleanFieldTrue(mv, owner, "hasCommercialLicense");

					mv.visitFieldInsn(
							Opcodes.GETSTATIC,
							"java/lang/Boolean",
							"TRUE",
							"Ljava/lang/Boolean;"
					);

					mv.visitMethodInsn(
							Opcodes.INVOKESTATIC,
							"java/util/concurrent/CompletableFuture",
							"completedFuture",
							"(Ljava/lang/Object;)Ljava/util/concurrent/CompletableFuture;",
							false
					);

					mv.visitInsn(Opcodes.ARETURN);
					mv.visitMaxs(0, 0);
					mv.visitEnd();
					return null;
				}

				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};

		reader.accept(visitor, 0);
		return writer.toByteArray();
	}

	private static void setBooleanFieldTrue(MethodVisitor mv, String owner, String name) {
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitFieldInsn(
				Opcodes.PUTSTATIC,
				owner,
				name,
				"Z"
		);
	}

}
