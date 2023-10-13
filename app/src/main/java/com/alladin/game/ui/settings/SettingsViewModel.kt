package com.alladin.game.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private val _currentPage = MutableLiveData(true)
    val currentPage: LiveData<Boolean> = _currentPage

    private val _currentLamp = MutableLiveData(0)
    val currentLamp: LiveData<Int> = _currentLamp

    private val _currentCharacter = MutableLiveData(0)
    val currentCharacter: LiveData<Int> = _currentCharacter

    fun left() {
        if (_currentPage.value!!) {
            if (_currentCharacter.value!! == 0) {
                _currentCharacter.postValue(1)
            } else {
                _currentCharacter.postValue(0)
            }
        } else {
            if (_currentLamp.value!! - 1 > 0) {
                _currentLamp.postValue(_currentLamp.value!! - 1)
            }
        }
    }

    fun right() {
        if (_currentPage.value!!) {
            if (_currentCharacter.value!! == 0) {
                _currentCharacter.postValue(1)
            } else {
                _currentCharacter.postValue(0)
            }
        } else {
            if (_currentLamp.value!! + 1 < 7) {
                _currentLamp.postValue(_currentLamp.value!! + 1)
            }
        }
    }

    fun changePage() {
        _currentPage.postValue(!_currentPage.value!!)
    }

    fun setCharacter(character: Int) {
        _currentCharacter.postValue(character)
    }

    fun setLamp(lamp: Int) {
        _currentLamp.postValue(lamp)
    }
}
