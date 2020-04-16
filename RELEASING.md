Releasing
========

 1. Make changes, get approval, merge to master, checkout master locally.
 2. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 3. Change VERSION_CODE to match semver, ie (`v3.0.1` -> `VERSION_CODE=301`).
 4. Update the `CHANGELOG.md` for the impending release.
 5. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 6. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 7. `./gradlew clean uploadArchives`
 8. Update the `gradle.properties` to the next SNAPSHOT version.
 9. `git commit -am "Prepare next development version."`
 10. `git push && git push --tags`
 11. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.
