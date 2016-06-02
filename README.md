# sbt-fresh #

sbt-fresh is a plugin for sbt to scaffold an opinionated fresh sbt project which is already prepared for multiple modules: It creates an sbt build according to established best practices, creates a useful package object for the root package, initializes a Git repository and creates an initial commit, etc.

Add sbt-fresh to your global plugins definition, which most probably resides under `~/.sbt/0.13/plugins/plugins.sbt`:

``` scala
addSbtPlugin("de.heikoseeberger" % "sbt-fresh" % "1.1.0")
```

You can define the following settings in your global build definition, which most probably sits at `~/.sbt/0.13/build.sbt`:

``` scala
import de.heikoseeberger.sbtfresh.FreshPlugin.autoImport._
freshAuthor       := "Heiko Seeberger"   // Author – value of "user.name" sys prop or "default" by default
freshName         := "no-idea"           // Build name – name of build directory by default
freshOrganization := "de.heikoseeberger" // Build organization – "default" by default
freshSetUpGit     := true                // Initialize a Git repo and create an initial commit – true by default
```

In order to scaffold a fresh sbt project, just start sbt in an empty directory. Then call the `fresh` command, optionally passing one or more of the following arguments which override the respective settings:
- `author`
- `name`
- `organization`
- `setUpGit`

## Layout

sbt-fresh creates a project with the following layout:

```
+ .gitignore
+ build.sbt             // specific settings for (single) module
+ NOTICE
+ project
--+ build.properties    // sbt version
--+ Build.scala         // common settings for all modules
--+ Dependencies.scala  // values for library dependencies
--+ plugins.sbt         // sbt-git, sbt-header, sbt-scalariform
+ README.md
+ shell-prompt.sbt      // show project id
+ src
--+ main
----+ resources
------+ LICENSE         // Apache 2 license
----+ scala
------+ package.scala   // type aliases repointing `Seq` and friends to immutable
```

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
