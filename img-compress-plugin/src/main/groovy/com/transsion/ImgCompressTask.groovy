package com.transsion

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.transsion.compressor.CompressorFactory
import com.transsion.util.FileUtils
import com.transsion.util.Logger
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.tasks.*

import java.nio.file.Files

class ImgCompressTask extends DefaultTask {
    @Input
    ImgCompressExtension config
    @Internal
    Logger log = Logger.getInstance(project.getProject())

    @Input
    List<String> sizeDirList = ["greater500KB", "200~500KB", "100~200KB", "50~100KB", "20~50KB", "less20KB"]

    @Internal
    ResultInfo resultInfo = new ResultInfo()

    ImgCompressTask() {
        description = 'ImgCompressTask'
        group = 'imgCompress'
        config = project.imgCompressOpt
    }

    @TaskAction
    def run() {
        log.i("ImgCompressTask run")

        if (!project == project.getProject()) {
            throw new IllegalArgumentException("img-compress-plugin must works on project level gradle")
        }
        def imgDirectories = getSourcesDirs(project)
        def compressedList = getCompressedInfo()
        def unCompressFileList = getUnCompressFileList(imgDirectories, compressedList)

        log.i("开始进入压缩任务")
        CompressorFactory.getCompressor(config.way, project).compress(project, unCompressFileList, config, resultInfo)
        copyToTestPath(unCompressFileList)
        updateCompressInfoList(unCompressFileList, compressedList)

        log.i("Task finish, compressed:${resultInfo.compressedSize} files  skip:${resultInfo.skipCount} Files  before total size: ${FileUtils.formetFileSize(resultInfo.beforeSize)}" +
                " after total size: ${FileUtils.formetFileSize(resultInfo.afterSize)} save size: ${FileUtils.formetFileSize(resultInfo.beforeSize - resultInfo.afterSize)}")
    }

    List<File> getSourcesDirs(Project root) {
        List<File> dirs = []
        root.allprojects { project ->
            log.i("ImgCompressTask deal ${project.name}")
            if (project.plugins.hasPlugin(AppPlugin)) {
                dirs.addAll(getSourcesDirsWithVariant(project.android.applicationVariants))
            } else if (project.plugins.hasPlugin(LibraryPlugin)) {
                dirs.addAll(getSourcesDirsWithVariant(project.android.libraryVariants))
            } else {
                log.i("ignore project:" + project.name)
            }
        }
        log.i("dirs size = ${dirs.size()}")
        return dirs
    }

    List<File> getSourcesDirsWithVariant(DomainObjectCollection<BaseVariant> collection) {
        List<File> imgDirectories = []
        collection.all { variant ->
            log.i("-------- variant: $variant.name --------")
            variant.sourceSets?.each { sourceSet ->
                if (sourceSet.resDirectories.empty) return
                sourceSet.resDirectories.each { res ->
                    if (res.exists() && res.listFiles() != null) {
                        res.eachDir {
                            if (it.directory && (it.name.startsWith("drawable") || it.name.startsWith("mipmap"))) {
                                if (!imgDirectories.contains(it)) {
                                    log.i("add dir $it")
                                    imgDirectories << it
                                }
                            }
                        }
                    }
                }
            }
        }
        return imgDirectories
    }

    @Internal
    List<CompressInfo> getCompressedInfo() {
        def compressedList = []
        def compressedListFile = project.file("${project.projectDir}/image-compressed-info.json")
        if (!compressedListFile.exists()) {
            compressedListFile.createNewFile()
        } else {
            try {
                def json = new FileInputStream(compressedListFile).getText("utf-8")
                def list = new Gson().fromJson(json, new TypeToken<ArrayList<CompressInfo>>() {}.getType())
                if (list instanceof ArrayList) compressedList = list
            } catch (Exception ignored) {
                log.i("compressed-resource.json is invalid, ignore")
            }
        }
        log.i("getCompressedInfo size=${compressedList.size()}")
        return compressedList
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    Iterable<File> getCompressedFiles() {
        def files = getCompressedInfo()
                .findAll { it?.path != null }
                .collect { new File(project.projectDir, it.path) }
                .findAll { it.exists() }
        return files
    }


    List<CompressInfo> getUnCompressFileList(List<File> imgDirectories, List<CompressInfo> compressedList) {
        List<CompressInfo> unCompressFileList = []

        dirFlag:
        for (File dir : imgDirectories) {
            fileFlag:
            for (File it : dir.listFiles()) {
                String fileName = it.name
                if (!config.whiteFiles.empty && config.whiteFiles.contains(fileName)) {
                    log.i("ignore whiteFiles >> ${it.absolutePath}")
                    continue fileFlag
                }
                def newMd5 = FileUtils.generateMD5(it)
                if (compressedList.any { info -> info.md5 == newMd5 }) {
                    log.i("ignore compressed >> ${it.absolutePath}")
                    continue fileFlag
                }
                if ((fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".webp")) &&
                        !fileName.contains(".9") && getPicSize(it) >= config.minSize) {
                    unCompressFileList.add(new CompressInfo(-1, -1, "", it.absolutePath, getOutputPath(it), newMd5))
                    log.i("add file  outputPath >> ${getOutputPath(it)}")
                }
            }
        }
        return unCompressFileList
    }

    String getOutputPath(File originImg) {
        if (!config.test) return originImg.getAbsolutePath()

        def testDir = new File("${project.projectDir}/ImageCompressTest")
        if (!testDir.exists()) {
            testDir.mkdir()
            sizeDirList.each {
                def dir = new File(testDir, it)
                if (!dir.exists()) dir.mkdir()
            }
        }
        def beforeSize = new FileInputStream(originImg).available()
        def fileSizeKB = beforeSize / 1024
        def originName = originImg.name
        def typeIndex = originName.indexOf(".")
        def testName = originName[0..<typeIndex] + "(test)" + originName[typeIndex..-1]

        def subDir = sizeDirList.find { s ->
            switch (s) {
                case "less20KB": return fileSizeKB < 20
                case "20~50KB": return fileSizeKB < 50
                case "50~100KB": return fileSizeKB < 100
                case "100~200KB": return fileSizeKB < 200
                case "200~500KB": return fileSizeKB < 500
                case "greater500KB": return fileSizeKB >= 500
            }
        } ?: "greater500KB"

        return "${project.projectDir}/ImageCompressTest/${subDir}/${testName}"
    }

    def updateCompressInfoList(List<CompressInfo> newList, List<CompressInfo> oldList) {
        def projectDirPath = project.projectDir.absolutePath
        newList.each { info ->
            info.path = info.path.replace(projectDirPath, "")
            info.outputPath = info.outputPath.replace(projectDirPath, "")
        }
        newList.each { newInfo ->
            def index = oldList.findIndexOf { it.md5 == newInfo.md5 }
            if (index >= 0) oldList[index] = newInfo else oldList.add(0, newInfo)
        }
        def json = new JsonOutput().toJson(oldList)
        new File("${project.projectDir}/image-compressed-info.json").write(JsonOutput.prettyPrint(json), "utf-8")
    }

    def copyToTestPath(List<CompressInfo> newList) {
        if (!config.test) return
        newList.each { info ->
            File origin = new File(info.path)
            File target = new File(new File(info.outputPath).parent, origin.name)
            if (target.exists()) target.delete()
            try {
                Files.copy(origin.toPath(), target.toPath())
            } catch (Exception e) {
                log.i("copyToTestPath error: ${e.message}")
            }
        }
    }

    int getPicSize(File file) {
        def fis = new FileInputStream(file)
        def size = fis.available()
        fis.close()
        return size / 1024
    }
}