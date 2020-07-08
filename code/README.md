This directory contains the code and scripts to run
the experiments described in the paper.
Please look at class `Main`, `MainTighterRestricted`,
and `MainRelaxedDistance` in `./src/main/java/` for the
implementation details. 

## Run

Clone the repository:

```bash
$ git clone https://github.com/presto-osu/sec20.git
$ cd sec20
```

Download and extract the [data](https://github.com/presto-osu/sec20/releases/tag/dataset) to folder `dataset`. Then run the following commands:

```bash
$ bash all.sh
$ bash all_attack.sh
```

The log files are stored in the `log` folder.
