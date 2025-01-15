package com.example.new_dopamind.ui.auth

import android.util.Log
import com.example.new_dopamind.data.model.LoginBody
import com.example.new_dopamind.data.model.LoginResponse
import com.example.new_dopamind.data.repository.UserRepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _loginResponse = MutableLiveData<LoginResponse>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String>()
    private val _exception = MutableLiveData<Boolean>()

    val loginResponse: LiveData<LoginResponse> = _loginResponse
    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String> = _errorMessage
    val exception: LiveData<Boolean> = _exception

    fun userLogin(loginBody: LoginBody) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userRepository.userLogin(loginBody)

                if (response.isSuccessful) {
                    _loginResponse.postValue(response.body())
                } else {
                    val errorJson = response.errorBody()?.string()
                    val apiError = Gson().fromJson(errorJson, LoginResponse::class.java)
                    _errorMessage.postValue(apiError?.message ?: "Login failed. Please check your email and password.")
                }
                _exception.postValue(false)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error: ${e.message}", e)
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