# PATCHiom

A simple CLI tool to patch out license checks and telemetry from [Axiom](https://axiom.moulberry.com/api).

> This software was made for educational and research purposes only.
> I do not encourage the unlawful use, modification, or distribution of third-party software.
> *But in fact, I would download a car!*

## Usage
In both usage types, `input` can be a file or directory.
If a directory is provided, it will be searched for an instance of the mod (by checking `fabric.mod.json` data).
The output, if provided, is relative to the execution location of the patcher.
### Interactive
Spawns a small interactive CLI for patching
```shell
java -jar ./patchiom.jar
```
### Raw
Use one simple command for patching, no interaction required

If no path for `output` argument is provided, the input file will be replaced
```shell
java -jar ./patchiom.jar <input> [output]
```