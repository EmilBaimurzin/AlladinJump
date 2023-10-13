package com.alladin.game.ui.alladin_jump

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alladin.game.core.library.GameViewModel
import com.alladin.game.core.library.XYIMpl
import com.alladin.game.core.library.random
import com.alladin.game.domain.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AlladinJumpViewModel : GameViewModel() {
    private val _platforms = MutableStateFlow<List<Platform>>(emptyList())
    val platforms = _platforms.asStateFlow()

    private val _coins = MutableStateFlow<List<XYIMpl>>(emptyList())
    val coins = _coins.asStateFlow()

    private val _lamps = MutableStateFlow<List<XYIMpl>>(emptyList())
    val lamps = _lamps.asStateFlow()

    private val _points = MutableLiveData(0)
    val points: LiveData<Int> = _points

    private val _pause = MutableLiveData(false)
    val pause: LiveData<Boolean> = _pause

    private val _lives = MutableLiveData(5)
    val lives: LiveData<Int> = _lives

    private val _trigger = MutableStateFlow(false)
    val trigger = _trigger.asStateFlow()

    private val _trigger2 = MutableStateFlow(false)
    val trigger2 = _trigger2.asStateFlow()

    private val _trigger3 = MutableStateFlow(false)
    val trigger3 = _trigger3.asStateFlow()

    var coinsCallback: (() -> Unit)? = null

    var coinsCount = 0

    private var canAddCoins = true

    private var canRemoveLife = true

    private var spawnDelay = 1800L
    private var isSpawning = false
    private var distance = 5

    private var jumpScope = CoroutineScope(Dispatchers.Default)
    private var spawnScope = CoroutineScope(Dispatchers.Default)
    private var moveScope = CoroutineScope(Dispatchers.Default)
    private var saveScope = CoroutineScope(Dispatchers.Default)

    var isGoingDown = true
    var isGoingLeft = false
    var isGoingRight = false
    var isGoingUp = false
    var isStopped = true
    var canGoDown = true

    var isInitial = true

    init {
        _playerXY.postValue(XYIMpl(0f, 0f))

        viewModelScope.launch {
            delay(6000)
            if (isInitial && !pauseState) {
                jump()
            }
            isInitial = false
        }
    }

    fun initPlayer(x: Float, y: Float) {
        _playerXY.postValue(XYIMpl(x, y))
    }

    fun changePause(state: Boolean) {
        _pause.postValue(state)
    }

    override fun stop() {
        spawnScope.cancel()
        gameScope.cancel()
        saveScope.cancel()
        jumpScope.cancel()
        moveScope.cancel()
    }

    fun start(
        maxX: Int,
        y1: Int,
        y2: Int,
        y3: Int,
        platformWidth: Int,
        playerHeight: Int,
        playerWidth: Int,
        platformHeight: Int,
        starSize: Int,
        lampSize: Int,
    ) {
        gameScope = CoroutineScope(Dispatchers.Default)
        saveScope = CoroutineScope(Dispatchers.Default)

        moveScope = CoroutineScope(Dispatchers.Default)
        isSpawning = false
        generatePlatforms(
            maxX,
            y1,
            y2,
            y3,
            platformWidth,
            starSize,
            lampSize
        )
        letEverythingMove(
            playerHeight,
            playerWidth,
            platformHeight,
            platformWidth,
            starSize,
            lampSize
        )
    }

    private fun generatePlatforms(
        maxX: Int,
        y1: Int,
        y2: Int,
        y3: Int,
        platformWidth: Int,
        starSize: Int,
        lampSize: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(spawnDelay)
                isSpawning = true
                spawnScope = CoroutineScope(Dispatchers.Default)
                moveScope.cancel()
                spawnScope.launch {
                    val currentList = _platforms.value.toMutableList()
                    val randomY = if (currentList.isNotEmpty()) {
                        val lastY = currentList.last().y
                        val platformNum = when {
                            lastY.toInt() == y1 -> 1
                            lastY.toInt() == y2 -> 2
                            else -> 3
                        }
                        val numList = mutableListOf(1, 2, 3)
                        numList.remove(platformNum)
                        numList.random()
                    } else 1 random 3
                    when (randomY) {
                        1 -> {
                            val platform = Platform(
                                maxX.toFloat(),
                                y1.toFloat(),
                            )

                            val currentStarsList = _coins.value.toMutableList()
                            val currentLampsList = _lamps.value.toMutableList()

                            repeat(1 random 2) {
                                currentStarsList.add(
                                    XYIMpl(
                                        (platform.x.toInt()..(platform.x.toInt() + platformWidth)).random()
                                            .toFloat(), platform.y - starSize
                                    )
                                )
                            }

                            currentLampsList.add(
                                XYIMpl(
                                    (platform.x.toInt()..(platform.x.toInt() + platformWidth)).random()
                                        .toFloat(), platform.y - lampSize
                                )
                            )

                            _coins.value = (currentStarsList)
                            _lamps.value = (currentLampsList)

                            _trigger2.value = !_trigger2.value
                            _trigger3.value = !_trigger3.value

                            currentList.add(platform)

                            _platforms.value = (currentList)
                            _trigger.value = !_trigger.value
                        }

                        2 -> {
                            val platform = Platform(
                                maxX.toFloat(),
                                y2.toFloat(),
                            )
                            val currentStarsList = _coins.value.toMutableList()
                            val currentLampsList = _lamps.value.toMutableList()

                            repeat(1 random 2) {
                                currentStarsList.add(
                                    XYIMpl(
                                        (platform.x.toInt()..(platform.x.toInt() + platformWidth)).random()
                                            .toFloat(), platform.y - starSize
                                    )
                                )
                            }

                            currentLampsList.add(
                                XYIMpl(
                                    (platform.x.toInt()..(platform.x.toInt() + platformWidth)).random()
                                        .toFloat(), platform.y - lampSize
                                )
                            )

                            _coins.value = (currentStarsList)
                            _lamps.value = (currentLampsList)

                            _trigger2.value = !_trigger2.value
                            _trigger3.value = !_trigger3.value

                            currentList.add(platform)

                            _platforms.value = (currentList)
                            _trigger.value = !_trigger.value
                        }

                        else -> {
                            val platform = Platform(
                                maxX.toFloat(),
                                y3.toFloat()
                            )

                            val currentStarsList = _coins.value.toMutableList()
                            val currentLampsList = _lamps.value.toMutableList()

                            repeat(1 random 2) {
                                currentStarsList.add(
                                    XYIMpl(
                                        (platform.x.toInt()..(platform.x.toInt() + platformWidth)).random()
                                            .toFloat(), platform.y - starSize
                                    )
                                )
                            }

                            currentLampsList.add(
                                XYIMpl(
                                    (platform.x.toInt()..(platform.x.toInt() + platformWidth)).random()
                                        .toFloat(), platform.y - lampSize
                                )
                            )

                            _coins.value = (currentStarsList)
                            _lamps.value = (currentLampsList)

                            _trigger2.value = !_trigger2.value
                            _trigger3.value = !_trigger3.value

                            currentList.add(platform)
                            _platforms.value = (currentList)
                            _trigger.value = !_trigger.value
                        }
                    }
                    moveScope = CoroutineScope(Dispatchers.Default)
                    isSpawning = false
                    spawnScope.cancel()
                }
            }
        }
    }

    fun teleportBack(playerHeight: Int, playerWidth: Int, platformWidth: Int) {
        val currentPlatforms = _platforms.value
        val lastPlatform = currentPlatforms.maxByOrNull { it.x }
        if (lastPlatform == null) {
            _lives.postValue(0)
        } else {
            val x = lastPlatform.x + (platformWidth / 2 - playerWidth / 2)
            val y = lastPlatform.y - playerHeight
            _playerXY.postValue(XYIMpl(x, y))

            if (canRemoveLife) {
                _lives.postValue(_lives.value!! - 1)
                canRemoveLife = false
                viewModelScope.launch {
                    delay(200)
                    canRemoveLife = true
                }
            }
        }
    }

    fun jump() {
        isInitial = false
        jumpScope.cancel()
        jumpScope = CoroutineScope(Dispatchers.Default)
        isStopped = false
        isGoingUp = true
        isGoingDown = false
        jumpScope.launch {
            repeat(10) { r ->
                repeat(5) {
                    delay(16)
                    val xy = _playerXY.value!!
                    if (isGoingLeft) {
                        xy.x = xy.x - distance / 2
                    }
                    if (isGoingRight) {
                        xy.x = xy.x + distance / 2
                    }
                    xy.y = xy.y - (10 - r + 1)
                    _playerXY.postValue(xy)
                }
            }

            isGoingDown = true

            repeat(10) { r ->
                repeat(5) {
                    delay(16)
                    val xy = _playerXY.value!!
                    if (isGoingLeft) {
                        xy.x = xy.x - distance / 2
                    }
                    if (isGoingRight) {
                        xy.x = xy.x + distance / 2
                    }
                    xy.y = xy.y + (r + 1)
                    _playerXY.postValue(xy)
                }
            }

            while (true) {
                delay(16)
                if (!isStopped) {
                    val xy = _playerXY.value!!
                    if (isGoingLeft) {
                        xy.x = xy.x - distance / 2
                    }
                    if (isGoingRight) {
                        xy.x = xy.x + distance / 2
                    }
                    xy.y = xy.y + (10)
                    _playerXY.postValue(xy)
                }
            }
        }
    }

    private fun letEverythingMove(
        playerHeight: Int,
        playerWidth: Int,
        platformHeight: Int,
        platformWidth: Int,
        starSize: Int,
        lampSize: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(10)
                if (!spawnScope.isActive && moveScope.isActive) {
                    moveScope.launch {
                        var needToFall = true
                        val currentList = _platforms.value.toMutableList()
                        val newList = mutableListOf<Platform>()
                        currentList.forEach { platform ->
                            //____________________________________________________
                            val platformY =
                                platform.y.toInt()..(platform.y + platformHeight).toInt()
                            val platformX = platform.x.toInt()..(platform.x + platformWidth).toInt()

                            val player = _playerXY.value!!

                            val playerY =
                                (player.y + playerHeight / 1.2).toInt()..(player.y + playerHeight).toInt()
                            val playerX = player.x.toInt()..(player.x + playerWidth).toInt()

                            if (playerY.any { it in platformY } && playerX.any { it in platformX } && isGoingDown) {
                                isStopped = true
                                jumpScope.cancel()
                                player.x = player.x - distance

                                if (isGoingRight) {
                                    player.x = player.x + distance * 2
                                }

                                needToFall = false

                                _playerXY.postValue(
                                    XYIMpl(
                                        player.x,
                                        platform.y - playerHeight
                                    )
                                )

                            }

                            //____________________________________________________
                            platform.x = platform.x - distance
                            if (platform.x + platformWidth > 0) {
                                newList.add(platform)
                            }
                        }
                        if (needToFall && !jumpScope.isActive) {
                            val xy = _playerXY.value!!
                            if (isGoingLeft) {
                                xy.x = xy.x - distance / 2
                            }
                            if (isGoingRight) {
                                xy.x = xy.x + distance / 2
                            }
                            _playerXY.postValue(XYIMpl(xy.x, xy.y + 5))
                        }
                        _platforms.value = (newList)
                        _trigger.value = !_trigger.value
                        val currentStarsList = _coins.value!!
                        val currentLampsList = _lamps.value!!

                        _coins.value = (
                            moveSomethingLeft(
                                starSize,
                                starSize,
                                playerWidth,
                                playerHeight,
                                currentStarsList.toMutableList(),
                                {
                                    viewModelScope.launch {
                                        if (canAddCoins) {
                                            coinsCallback?.invoke()
                                            coinsCount += 1
                                            canAddCoins = false
                                            delay(200)
                                            canAddCoins = true
                                        }
                                    }
                                },
                                {},
                                distance
                            ).toList() as List<XYIMpl>
                        )

                        _lamps.value = (
                            moveSomethingLeft(
                                lampSize,
                                lampSize,
                                playerWidth,
                                playerHeight,
                                currentLampsList.toMutableList(),
                                {
                                    _points.postValue(_points.value!! + 1)
                                },
                                {},
                                distance
                            ).toList() as List<XYIMpl>
                        )

                        _trigger2.value = !_trigger2.value
                        _trigger3.value = !_trigger3.value
                    }
                }
            }
        }
    }

    fun stopMoving() {
        isGoingLeft = false
        isGoingRight = false
    }

    fun goDown(downDistance: Int) {
        viewModelScope.launch {
            if (isStopped && canGoDown) {
                canGoDown = false
                _playerXY.postValue(XYIMpl(_playerXY.value!!.x, _playerXY.value!!.y + downDistance))
                delay(500)
                canGoDown = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        jumpScope.cancel()
    }
}