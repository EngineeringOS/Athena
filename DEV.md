

\## Tips





manifesto is  submodule of Athena(DONE)



```

git submodule add https://github.com/EngineeringOS/manifesto.git manifesto

git add .gitmodules manifesto

git commit -m "Add manifesto submodule"



```



every time to pull latest with manifesto



```

git pull \&\& git submodule update --remote manifesto

```


build bootstrap commands

```

java25

.\gradlew.bat build

.\gradlew.bat test

.\gradlew.bat :cli:run --args="--help"

```


workspace note

```

manifesto/ remains a git submodule and reference input.

it is not part of the Gradle module graph.

```

