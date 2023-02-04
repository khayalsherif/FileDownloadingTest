package com.vholodynskyi.filedownloadingtest.presentation.downloading

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vholodynskyi.filedownloadingtest.domain.use_case.DownloadFileUseCase
import com.vholodynskyi.filedownloadingtest.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val downloadFileUseCase: DownloadFileUseCase
) : ViewModel() {

    private val _state = mutableStateOf(DownloadingState())
    val state: State<DownloadingState> = _state

    private val fileUrl = "https://www.africau.edu/images/default/sample.pdf" // DON'T FORGET TO SET YOUR URL

    private val testJob: Job

    init {
        testJob = downloadFiles()
    }

    private fun downloadFiles(): Job {
        return downloadFileUseCase(fileUrl)
            .onEach { result ->
                _state.value = when (result) {
                    is Resource.Error -> DownloadingState(error = result.message ?: "")
                    is Resource.Loading -> DownloadingState(isLoading = true)
                    is Resource.Success -> DownloadingState(file = result.data ?: "")
                }
            }.onCompletion {
                if (it is CancellationException) {
                    _state.value = DownloadingState(error = "Downloading is cancelled")
                }
            }.launchIn(viewModelScope)
    }

    fun cancelDownloading() {
        if (testJob.isActive) {
            testJob.cancel()
        }
    }
}