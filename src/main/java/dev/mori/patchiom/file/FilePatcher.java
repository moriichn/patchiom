package dev.mori.patchiom.file;

import dev.mori.patchiom.log.OutputAdapter;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class FilePatcher {

	public static void patchJar(Path inputJar, Path outputJar, OutputAdapter log) throws IOException {
		log.info("Reading jar..");
		try (
				JarFile jarFile = new JarFile(inputJar.toFile());
				OutputStream outputStream = Files.newOutputStream(outputJar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				JarOutputStream jarOutput = new JarOutputStream(outputStream)
		) {
			log.info("Iterating entries..");
			Enumeration<JarEntry> entries = jarFile.entries();
			boolean patched = false;

			while (entries.hasMoreElements()) {
				JarEntry originalEntry = entries.nextElement();

				JarEntry newEntry = new JarEntry(originalEntry.getName());
				newEntry.setTime(originalEntry.getTime());

				if (originalEntry.getComment() != null) {
					newEntry.setComment(originalEntry.getComment());
				}

				if (originalEntry.getExtra() != null) {
					newEntry.setExtra(originalEntry.getExtra());
				}

				jarOutput.putNextEntry(newEntry);

				try (InputStream in = jarFile.getInputStream(originalEntry)) {
					if ("com/moulberry/axiom/utils/Authorization.class".equals(originalEntry.getName())) {
						log.info("Reading class " + originalEntry.getName());
						byte[] originalBytes = in.readAllBytes();
						log.info("Patching class " + originalEntry.getName());
						byte[] patchedBytes = patchClass(originalBytes);
						log.info("Writing patched class..");
						jarOutput.write(patchedBytes);
						log.info("Written patched class to output");
						patched = true;
					} else {
						in.transferTo(jarOutput);
					}
				}

				jarOutput.closeEntry();
			}

			if(!patched) {
				throw new IllegalStateException("Can't find target class in jar");
			}
		}
	}

	private static byte[] patchClass(byte[] classBytes) {
		ClassReader reader = new ClassReader(classBytes);
		ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

		ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				Type sType = Type.getReturnType(descriptor);
				if (name.equals("checkServer") && sType.equals(Type.getType(CompletableFuture.class))) {

					MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
					mv.visitCode();

					mv.visitFieldInsn(
							Opcodes.GETSTATIC,
							"com/moulberry/axiom/utils/Authorization$ServerAuthorization",
							"COMMERCIAL",
							"Lcom/moulberry/axiom/utils/Authorization$ServerAuthorization;"
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

}
