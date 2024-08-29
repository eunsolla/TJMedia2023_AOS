package com.verse.app.usecase

import com.verse.app.contants.ResourcePathType
import com.verse.app.model.base.BaseResponse
import com.verse.app.model.common.GetResourcePathResponse
import com.verse.app.model.lounge.LoungeData
import com.verse.app.model.param.LoungeBody
import com.verse.app.repository.http.ApiService
import com.verse.app.utility.provider.FileProvider
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

/**
 * Description : 중간에 로직 구성이 복잡해서 UseCase 만듦
 *
 * Created by juhongmin on 2023/05/20
 */
class PostLoungeUseCase @Inject constructor(
    private val apiService: ApiService,
    private val fileProvider: FileProvider
) {

    sealed class MiddleModel(
        open val baseData: LoungeData
    ) {
        data class Step1(
            val uploadMap: MutableMap<Int, String>,
            override val baseData: LoungeData
        ) : MiddleModel(baseData)

        data class Step2(
            val imageMap: MutableMap<Int, String>, // 로쿨 이미지 URI
            val uploadPathList: List<String>, // 업로드 할 이미지 위치값
            override val baseData: LoungeData
        ) : MiddleModel(baseData)

        data class Step3(
            val mergeImagePathList: List<String>,
            override val baseData: LoungeData
        ) : MiddleModel(baseData)
    }

    operator fun invoke(data: LoungeData): Single<BaseResponse> {
        return Single.create { emitter ->
            val uploadMap = mutableMapOf<Int, String>()
            data.imageList.forEachIndexed { idx, data ->
                if (!data.localUri.isNullOrEmpty()) {
                    uploadMap.put(idx, data.localUri)
                }
            }
            emitter.onSuccess(MiddleModel.Step1(uploadMap, data))
        }.flatMap { reqImagePaths(it) }
            .flatMap { reqUploadImages(it) }
            .flatMap { reqPostContents(it) }
    }

    private fun reqImagePaths(model: MiddleModel.Step1): Single<MiddleModel.Step2> {
        return apiService.getResourcePath(
            resType = ResourcePathType.LOUNGE.code,
            contentsCd = if (model.baseData.code.isNullOrEmpty()) null else model.baseData.code,
            updateImageCount = 1.coerceAtLeast(model.uploadMap.size)
        ).map { toStep2(model, it) }
    }

    private fun toStep2(model: MiddleModel.Step1, res: GetResourcePathResponse): MiddleModel.Step2 {
        if (model.baseData.code.isNullOrEmpty()) {
            model.baseData.code = res.result.louMngCd
        }
        return if (model.uploadMap.isEmpty()) {
            MiddleModel.Step2(
                mutableMapOf(),
                listOf(),
                model.baseData
            )
        } else {
            MiddleModel.Step2(
                model.uploadMap,
                res.result.attImgPathList,
                model.baseData
            )
        }
    }

    private fun reqUploadImages(
        model: MiddleModel.Step2
    ): Single<MiddleModel.Step3> {
        return if (model.uploadPathList.isEmpty()) {
            Single.just(MiddleModel.Step3(
                model.baseData.imageList
                    .filter { it.imagePath != null }
                    .map { it.imagePath!! }
                    .toList(),
                model.baseData
            ))
        } else {
            // First 로컬 이미지, Second 업로드할 경로
            val list = mutableListOf<Pair<String, String>>()
            model.imageMap.map { it.value }.forEachIndexed { index, s ->
                list.add(s to model.uploadPathList.get(index))
            }
            fileProvider.requestImageUploads(list)
                .map { getIndexMatchingImageList(model, it) }
        }
    }

    private fun getIndexMatchingImageList(
        model: MiddleModel.Step2,
        uploadList: List<String>
    ): MiddleModel.Step3 {
        val list = mutableListOf<String>()
        var cnt = 0
        model.baseData.imageList.forEachIndexed { index, data ->
            // 갤러리에서 가져온 이미지 데이터인경우
            if (!data.localUri.isNullOrEmpty()) {
                list.add(uploadList[cnt])
                cnt++
            } else if (!data.imagePath.isNullOrEmpty()) {
                list.add(data.imagePath)
            }
        }
        return MiddleModel.Step3(
            mergeImagePathList = list,
            baseData = model.baseData
        )
    }

    private fun reqPostContents(model: MiddleModel.Step3): Single<BaseResponse> {
        return apiService.postLoungeContents(LoungeBody(model))
    }
}
