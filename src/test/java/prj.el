; project file for jdee version 2.4.2 
; caution: this project file may not be customized via emacs: 
; - the ordering is lost 
; - backquotes are eliminated which are needed for unquoting! 
; better would be a revival of the maven-jdee-plugin 
;tstsrc
(jdee-project-file-version "1.0")
(jdee-set-variables

; general 
 '(jdee-sourcepath (quote ("../../../src/main/java" 
			   "../../../src/test/java")))
 '(jdee-global-classpath 
    `("../../../target/test-classes" 
      "../../../target/classes" 
      ,@(jdee-maven-classpath-from-file "../../../target/test.cp")
      )
   )
 '(jdee-resolve-relative-paths-p t)
 '(jdee-enable-abbrev-mode t)

; jdk 
 '(jdee-jdk (quote "1.8"))
 '(jdee-jdk-registry (quote (("1.8" . "/usr/lib64/jvm/javaLatest/"))))
 '(jdee-jdk-doc-url "file:///usr/share/javadoc/java-1.8.0-openjdk/api/index.html")

; code generation 
 '(jdee-gen-comments nil)

; compilation 
 '(jdee-compiler (quote ("javac" "")))
 '(jdee-compile-enable-kill-buffer t)
 '(jdee-compile-option-encoding "UTF-8")
 '(jdee-compile-option-sourcepath (quote ("../../../src/main/java" 
					  "../../../src/test/java")))
 '(jdee-compile-option-command-line-args (quote ("-Xlint")))
 '(jdee-compile-option-depend nil)
 '(jdee-compile-option-deprecation t)
; '(jdee-compile-option-debug (t t t))
 '(jdee-compile-option-directory "../../../target/test-classes")

; running jvm 
 '(jdee-run-option-hotspot-type (quote server))
 '(jdee-run-option-enable-system-assertions t)
 '(jdee-run-option-disable-assertions "Nowhere")
 '(jdee-run-option-enable-assertions "Everywhere")
 '(jdee-run-option-vm-args (quote ("-Xfuture")))
; '(jdee-run-option-verbose (t t t))
; '(jdee-run-option-verify (t t))
 '(jdee-run-option-properties 
   `(;("sourcepath"      . ,(concat jdee-maven-project-dir "src/main/java:" 
     ;	                            jdee-maven-project-dir "src/test/java"))

     ("tstModelDir"     . ,(concat jdee-maven-project-dir "target/test-classes/" "../filteredTestResources/")) 
     )
   )

; debugger 
 '(jdee-debugger (quote ("jdb")))
; three flags: classes loaded, memory freed, JNI info 
 '(jdee-db-option-verbose (quote (nil nil nil)))
 '(jdee-db-log-debugger-output-flag t)
 '(jdee-db-option-properties 
   `(("sourcepath"      . ,(concat jdee-maven-project-dir "src/main/java:" 
				   jdee-maven-project-dir "src/test/java"))
     ("chooseClasspath" . ,(concat jdee-maven-project-dir "target/test-classes"))
     ("environment"     . ,jdee-maven-project-dir) 
     ("srcDir"          . "src/main/java/") 
     ("srcAntlr4Dir"    . "src/main/antlr4/") 
     ("tstSrcFpgaDir"   . "src/test/fpga/") 
     ("octaveDir"       . "/usr/share/octave/latest/") 
     ("mCompilerProps"  . ,(concat jdee-maven-project-dir "src/main/resources/octave/compiler.props")) 
     ("noClsReload"     . "dk.ange.octave.OctaveEngine:dk.ange.octave.type.cast.Caster:dk.ange.octave.type.Octave"))
   )

; javadoc 
 '(jdee-javadoc-check-undeclared-exception-flag t)
 '(jdee-javadoc-checker-level (quote private))
 '(jdee-javadoc-gen-detail-switch (quote ("-private")))
 '(jdee-javadoc-gen-use t)
 '(jdee-javadoc-gen-destination-directory "../../../target/docs/apidocs")

; build 
 '(jdee-build-function (quote jdee-maven-build))

; ant 
; '(jdee-ant-home "/usr/share/ant/lib/")
 `(jdee-ant-working-directory ,jdee-maven-project-dir)
 '(jdee-ant-enable-find t)
 '(jdee-ant-read-target t)
 '(jdee-ant-invocation-method (quote ("Java")))
 '(jdee-ant-use-global-classpath t)

; misc 
 '(jdee-server-dir "~/.emacs.d/jdee-server-jar")
 '(jdee-help-docsets 
    `(("User (javadoc)" "file:///usr/java/latest/MyApiDocs"            nil)
      ("User (javadoc)" 
       ,(concat        "file://" jdee-maven-project-dir "target/docs") nil)
     )
   )
)
