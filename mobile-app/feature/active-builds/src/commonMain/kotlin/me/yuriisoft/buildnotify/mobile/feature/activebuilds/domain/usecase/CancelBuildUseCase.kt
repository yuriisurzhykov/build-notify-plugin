package me.yuriisoft.buildnotify.mobile.feature.activebuilds.domain.usecase

interface CancelBuildUseCase {
    suspend operator fun invoke(buildId: String)
}
