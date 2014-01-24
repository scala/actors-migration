import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.site.JekyllSupport.Jekyll
import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit.git

site.settings

ghpages.settings

site.includeScaladoc()

site.jekyllSupport()

git.remoteRepo := "git@github.com:scala/actors-migration.git"

includeFilter in Jekyll := ("*.html" | "*.png" | "*.js" | "*.css" | "CNAME")

// the migration guide goes to scaladoc
excludeFilter in Jekyll := ("actors-migration-guide.html")
