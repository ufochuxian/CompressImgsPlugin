package com.transsion.util

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project;


class CwebpUtil {

    private static final def name = "cwebp";

    static def copyCwebp2BuildFolder(Project project) {
        def cwebpDir = getCwebpDirectory(project)
        if (!cwebpDir.exists()) {
            cwebpDir.mkdirs()
        }
        def pngFile = new File(getCwebpFilePath(project))
        if (!pngFile.exists()) {
            new FileOutputStream(pngFile).withStream {
                def inputStream = CwebpUtil.class.getResourceAsStream("/$name/${getFilename()}")
                it.write(inputStream.getBytes())
            }
        }
        pngFile.setExecutable(true, false)
    }

    /**
     * .../build/cwebp
     * @return String
     */
    private static def getCwebpDirectoryPath(Project project) {
        return project.buildDir.absolutePath + File.separator + "$name"
    }

    /**
     * .../build/cwebp
     * @return File (Directory)
     */
    private static def getCwebpDirectory(Project project) {
        return new File(getCwebpDirectoryPath(project))
    }

    /**
     * .../build/cwebp/{cwebp/cwebp-mac/cwebp.exe}.
     *
     * @return String
     */
    static def getCwebpFilePath(Project project) {
        return getCwebpDirectoryPath(project) + File.separator + getFilename()
    }

    static def getFilename() {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            return "${name}.exe"
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            return "${name}-mac"
        } else {
            return "$name"
        }
    }

}