package de.royzer.fabrichg.kit.kits

import de.royzer.fabrichg.TEXT_BLUE
import de.royzer.fabrichg.TEXT_GRAY
import de.royzer.fabrichg.data.hgplayer.HGPlayer
import de.royzer.fabrichg.data.hgplayer.hgPlayer
import de.royzer.fabrichg.kit.kit
import de.royzer.fabrichg.kit.property.property
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.Items
import net.silkmc.silk.core.text.literalText
import java.util.UUID
import kotlin.random.Random

private const val ZICKZACK_COMBO_KEY = "zickzackCombo"


val zickzackKit = kit("Zickzack") {
    kitSelectorItem = Items.DIAMOND_BLOCK.defaultInstance

    description = "BastiGHG"

    val minCombo by property(3, "Min combo")
    val chanceMultiplier by property(8, "Dodge probability (combo * this)")

    onEnable { hgPlayer, kit, serverPlayer ->
        hgPlayer.playerData[ZICKZACK_COMBO_KEY] = hashMapOf<UUID, Int>()
    }

    onDisable { hgPlayer, kit ->
        hgPlayer.getPlayerData<HashMap<UUID, Int>>(ZICKZACK_COMBO_KEY)?.clear()
        hgPlayer.serverPlayer?.playNotifySound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.PLAYERS, 1f, 1f)
    }

    info { hgPlayer, kit ->
        literalText {
            val lastAttackedPlayer = hgPlayer.serverPlayer?.lastHurtMob?.hgPlayer ?: return@info null

            val combo = hgPlayer.combo(lastAttackedPlayer.uuid)

            text("Combo (${lastAttackedPlayer.name}): ") { color = TEXT_GRAY }
            text(combo.toString()) { color = TEXT_BLUE }
        }
    }

    kitEvents {
        afterDamagePlayer { hgPlayer, kit, hittedPlayer ->
            val combo = hgPlayer.combo(hittedPlayer.uuid)
            hgPlayer.getPlayerData<HashMap<UUID, Int>>(ZICKZACK_COMBO_KEY)
                ?.set(hittedPlayer.uuid, combo + 1) // muss != null sein
                                                    // eh nicht

            hgPlayer.updateScoreboard()
        }

        // return ob gecancelt werden soll
        onAttackedByPlayer { hgPlayer, kit, attacker ->
            val serverPlayer = hgPlayer.serverPlayer ?: return@onAttackedByPlayer false
            val combo = hgPlayer.combo(attacker.uuid)

            if (combo > minCombo) {
                // bei 3er combo 24% bei 10er 80% und dann pro dodge 1 runter
                // und wenn ein hit durch geht reset
                if (Random.nextInt(100) < Math.clamp(combo * chanceMultiplier.toLong(), 0, 70)) {
                    hgPlayer.getPlayerData<HashMap<UUID, Int>>(ZICKZACK_COMBO_KEY)?.set(attacker.uuid, combo - 1)
                    serverPlayer.playNotifySound(
                        SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
                        SoundSource.PLAYERS,
                        1f,
                        1f
                    )
                    attacker.playNotifySound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.PLAYERS, 1f, 1f)

                    hgPlayer.updateScoreboard()
                    return@onAttackedByPlayer true
                } else {
                    hgPlayer.getPlayerData<HashMap<UUID, Int>>(ZICKZACK_COMBO_KEY)?.set(attacker.uuid, 0)
                }
            }

            hgPlayer.updateScoreboard()
            return@onAttackedByPlayer false

        }
    }
}

private fun HGPlayer.combo(uuid: UUID): Int = getPlayerData<HashMap<UUID, Int>>(ZICKZACK_COMBO_KEY)?.get(uuid) ?: 0
