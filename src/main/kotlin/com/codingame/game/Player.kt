package com.codingame.game

import com.codingame.game.Constants.QUEEN_HP
import com.codingame.game.Constants.QUEEN_VISION
import com.codingame.game.Constants.STARTING_GOLD
import com.codingame.gameengine.core.AbstractPlayer
import kotlin.math.roundToInt

class Player : AbstractPlayer() {
  override fun getExpectedOutputLines(): Int = 2
  lateinit var queenUnit: Queen
  lateinit var enemyPlayer: Player
  var isSecondPlayer: Boolean = false

  private fun fixOwner(player: Player?) = when (player) { null -> -1; this -> 0; else -> 1 }

  fun printObstacleInit(obstacle: Obstacle) {
    val (x,y) = obstacle.location
    val toks = listOf(obstacle.obstacleId, x.roundToInt(), y.roundToInt(), obstacle.radius)
    sendInputLine(toks.joinToString(" "))
  }

  fun printObstaclePerTurn(obstacle: Obstacle) {
    val struc = obstacle.structure
    val visible = (struc != null && struc.owner == this) || obstacle.location.distanceTo(queenUnit.location) < QUEEN_VISION

    val toks = listOf(
        obstacle.obstacleId,
        if (visible) obstacle.gold else -1,
        if (visible) obstacle.maxMineSize else -1) + when (struc) {
      is Mine -> listOf(0, fixOwner(struc.owner), if (visible) struc.incomeRate else -1, -1)
      is Tower -> listOf(1, fixOwner(struc.owner), struc.health, struc.attackRadius)
      is Barracks -> listOf(2, fixOwner(struc.owner), if(!struc.isTraining) 0 else struc.progressMax - struc.progress, struc.creepType.ordinal)
      else -> listOf(-1, -1, -1, -1)
    }
    sendInputLine(toks.joinToString(" "))
  }

  val activeCreeps = mutableListOf<Creep>()

  fun allUnits() = activeCreeps + queenUnit

  var health by nonNegative<Player>(100).andAlso { if (score >= 0) score = it }
  var gold = STARTING_GOLD
  var goldPerTurn = 0

  fun checkQueenHealth() {
    queenUnit.health = health
    if (health == 0) deactivate("Dead queen")
    hud.update()
  }

  val hud by lazy { PlayerHUD(this, isSecondPlayer) }

  fun kill(reason: String) {
    score = -1
    deactivate(reason)
  }
}