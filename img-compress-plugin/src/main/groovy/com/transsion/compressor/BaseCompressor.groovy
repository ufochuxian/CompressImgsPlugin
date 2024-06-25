import com.transsion.CompressInfo
import com.transsion.ImgCompressExtension
import com.transsion.ResultInfo
import com.transsion.compressor.ICompressor
import org.gradle.api.Project

class BaseCompressor implements ICompressor {

    @Override
    void compress(Project rootProject, List<CompressInfo> unCompressFileList, ImgCompressExtension config, ResultInfo resultInfo) {

    }

    def deleteSourceFiles(String sourcePath) {
        //delete original img
        def deleteOriginImgResult = Files.deleteIfExists(Paths.get(sourcePath))
        println("Deleted original PNG image: ${inputImagePath},deleteOriginImgResult:${deleteOriginImgResult}")
    }
}