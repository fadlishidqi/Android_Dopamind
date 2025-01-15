package com.example.new_dopamind.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_dopamind.data.model.RegisterBody
import com.example.new_dopamind.data.model.RegisterResponse
import com.example.new_dopamind.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()
    private val _exception = MutableLiveData<Boolean>()

    val registerResponse: LiveData<RegisterResponse> = _registerResponse
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String> = _errorMessage
    val exception: LiveData<Boolean> = _exception

    fun userRegister(registerBody: RegisterBody) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.userRegister(registerBody)

                if (response.isSuccessful) {
                    _registerResponse.postValue(response.body())
                } else {
                    val errorJson = response.errorBody()?.string()
                    val apiError = Gson().fromJson(errorJson, RegisterResponse::class.java)
                    _errorMessage.postValue(apiError.message)
                }
                _exception.postValue(false)
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error: ${e.message}", e)
                _exception.postValue(true)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun resetExceptionValue() {
        _exception.value = false
    }
}