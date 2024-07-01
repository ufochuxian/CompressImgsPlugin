package com.transsion.compressor


import com.transsion.ResultInfo
import com.transsion.util.FileUtils
import com.transsion.util.Logger
import com.transsion.CompressInfo
import com.transsion.ImgCompressExtension
import org.gradle.api.Project
import com.tinify.*
import org.gradle.api.tasks.Input

class TinyCompressor extends BaseCompressor {
    int keyIndex = 0
    @Input
    def compressInfoList = new ArrayList<CompressInfo>()
    boolean accountError = false
    @Input
    ImgCompressExtension config;
    long beforeTotalSize = 0
    long afterTotalSize = 0
    Logger log

    @Override
    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo) {
        this.rootProject = rootProject;
        this.compressInfoList = compressInfoList;
        this.config = config
        log = Logger.getInstance(rootProject)
        log.i("使用TinyPng进行压缩")
        checkKey()
        unCompressFileList.each {
            tryCompressSingleFile(it)
        }
//        println("Task finish, compress ${unCompressFileList.size()} files, before total size: ${FileUtils.formetFileSize(beforeTotalSize)} after total size: ${FileUtils.formetFileSize(afterTotalSize)}")
        resultInfo.compressedSize = unCompressFileList.size()
        resultInfo.beforeSize = beforeTotalSize
        resultInfo.afterSize = afterTotalSize
        resultInfo.skipCount = 0

    }

    def tryCompressSingleFile(CompressInfo info) {

        println("find target pic >>>>>>>>>>>>> ${info.path}")
        try {
            def fis = new FileInputStream(new File(info.path))
            //available在读取之前知道数据流有多少个字节,即原始文件大小
            def beforeSize = fis.available()
            def beforeSizeStr = FileUtils.formetFileSize(beforeSize)

            // Use the Tinify API client
            def tSource = Tinify.fromFile(info.path)
            tSource.toFile(info.outputPath)


            //处理转换webp的信息&删除源文件
            super.onCompressed(info, {
                fis = new FileInputStream(new File(info.outputPath))
                //这里没对压缩后如果文件变大做处理
                def afterSize = fis.available()
                def afterSizeStr = FileUtils.formetFileSize(afterSize)
                beforeTotalSize += beforeSize
                afterTotalSize += afterSize
                //记录md5的信息，必须在转换完webP之后，如果更新完信息之后，再生成md5，那么就会发生，再次执行“压缩任务之后”，同名文件的md5信息不一致的问题啦
                info.update(beforeSize, afterSize, FileUtils.generateMD5(new File(info.outputPath)))
                log.i("beforeSize: $beforeSizeStr -> afterSize: ${afterSizeStr} radio:${info.ratio}")
            })
        } catch (AccountException e) {
            println("AccountException: ${e.getMessage()}")
            if (config.tinyKeys.size() <= ++keyIndex){
                accountError = true
                return
            }else {
                //失败重试
                Tinify.setKey(config.tinyKeys[keyIndex])
                tryCompressSingleFile(file)
            }
            // Verify your API key and account limit.
        } catch (ClientException e) {
            // Check your source image and request options.
            println("ClientException: ${e.getMessage()}")
        } catch (ServerException e) {
            // Temporary issue with the Tinify API.
            println("ServerException: ${e.getMessage()}")
        } catch (ConnectionException e) {
            // A network connection error occurred.
            println("ConnectionException: ${e.getMessage()}")
        } catch (IOException e) {
            // Something else went wrong, unrelated to the Tinify API.
            println("IOException: ${e.getMessage()}")
        } catch (Exception e) {
            println("Exception: ${e.toString()}")
        }
    }

    def checkKey(){
        if (config.tinyKeys.empty){
            println("Tiny tinyKeys is empty.")
            return
        }
        try {
            //测试key值的正确性
            Tinify.setKey("${config.tinyKeys[keyIndex]}")
            Tinify.validate()
        } catch (AccountException ex) {
            println("TinyCompressor" + ex.printStackTrace())
        }
    }

    static void main(String[] args) {
        convertToWebP("/Users/chenjianxiang/Documents/opensource/CompressImgsPlugin/app/src/main/res/drawable/test_pic4.png")
    }




}