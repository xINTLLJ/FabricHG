package de.royzer.fabrichg.kit.kits

import de.royzer.fabrichg.TEXT_BLUE
import de.royzer.fabrichg.TEXT_GRAY
import de.royzer.fabrichg.kit.achievements.delegate.achievement
import de.royzer.fabrichg.kit.kit
import de.royzer.fabrichg.kit.property.property
import de.royzer.fabrichg.mixinskt.SoupHealingKt.soupHealing
import de.royzer.fabrichg.util.giveOrDropItem
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Items
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setCustomName
import net.silkmc.silk.core.text.literalText
import kotlin.math.round
import kotlin.math.sqrt

private val perfectSoup get() = itemStack(Items.RABBIT_STEW) {
    setCustomName("Perfect Stew") {
        italic = false
    }
}

val perfectKit = kit("Perfect") {
    kitSelectorItem = Items.BEACON.defaultInstance

    description = "Get rewarded for not presouping"

    val streakKey = "perfectStreak"

    val soupsForReward by property(8, "soups for reward")

    val soupPerfectAchievement by achievement("soup perfekt") {
        level(200)
        level(1000)
        level(5000)
    }
    val soupPerfect25StreakAchievement by achievement("soup perfekt 25er streak") {
        level(200)
        level(1000)
        level(5000)
    }

    kitEvents {
        onSoupEat { hgPlayer, kit, item ->
            val serverPlayer = hgPlayer.serverPlayer ?: return@onSoupEat
            val streak = hgPlayer.getPlayerData<Int>(streakKey) ?: 0

            val presouped =
                (serverPlayer.health + item.soupHealing) > serverPlayer.attributes.getBaseValue(Attributes.MAX_HEALTH)

            if (!presouped) {
                hgPlayer.playerData[streakKey] = streak + 1
                kit.currentInfo = literalText {
                    text("Perfect Streak: ") { TEXT_GRAY}
                    text((streak + 1).toString()) { TEXT_BLUE }
                }
                soupPerfectAchievement.awardLater(serverPlayer)

                if (streak + 1 == 25) {
                    soupPerfect25StreakAchievement.awardLater(serverPlayer)
                }
                if ((streak + 1) % soupsForReward == 0) {
                    val soupsToBeAdded =
                        round(sqrt(((streak + 1) / soupsForReward).toDouble() * 0.8)).toInt() + 1 // round(sqrt(x*0.8))+1 auf https://www.geogebra.org/calculator
                    repeat(soupsToBeAdded) {
                        serverPlayer.giveOrDropItem(perfectSoup)
                        serverPlayer.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER,1f, 1f)
                    }
                }

            } else {
                if (streak > 1) {
                    serverPlayer.playNotifySound(SoundEvents.DONKEY_DEATH, SoundSource.MASTER,1f, 1f)
                }
                hgPlayer.playerData[streakKey] = 0
                kit.currentInfo = literalText {
                    text("Perfect Streak: ") { TEXT_GRAY }
                    text("0") { TEXT_BLUE }
                }
            }
        }
    }
}