package com.polar.androidcommunications.api.ble.model.gatt.client.pmd.model

import com.polar.androidcommunications.api.ble.model.gatt.client.pmd.BlePMDClient.PmdDataFrameType
import com.polar.androidcommunications.common.ble.BleUtils
import java.util.*

class EcgData internal constructor(@JvmField val timeStamp: Long) {

    data class EcgSample internal constructor(
        // samples in signed microvolts
        val timeStamp: Long,

        @JvmField
        val microVolts: Int,
        val overSampling: Boolean = false,
        val skinContactBit: Byte = 0,
        val contactImpedance: Byte = 0,
        val ecgDataTag: Byte = 0,
        val paceDataTag: Byte = 0,
    )


    @JvmField
    val ecgSamples: MutableList<EcgSample> = ArrayList()

    companion object {
        fun parseDataFromDataFrame(isCompressed: Boolean, frameType: PmdDataFrameType, frame: ByteArray, factor: Float, timeStamp: Long): EcgData {
            return if (isCompressed) {
                throw java.lang.Exception("Compressed FrameType: $frameType is not supported by EcgData data parser")
            } else {
                when (frameType) {
                    PmdDataFrameType.TYPE_0 -> dataFromRawType0(frame, timeStamp)
                    PmdDataFrameType.TYPE_1 -> dataFromRawType1(frame, timeStamp)
                    PmdDataFrameType.TYPE_2 -> dataFromRawType2(frame, timeStamp)
                    else -> throw java.lang.Exception("Raw FrameType: $frameType is not supported by EcgData data parser")
                }
            }
        }

        private fun dataFromRawType0(value: ByteArray, timeStamp: Long): EcgData {
            val ecgData = EcgData(timeStamp)
            var offset = 0
            while (offset < value.size) {
                val microVolts = BleUtils.convertArrayToSignedInt(value, offset, 3)
                offset += 3
                ecgData.ecgSamples.add(EcgSample(timeStamp = timeStamp, microVolts = microVolts))
            }
            return ecgData
        }

        private fun dataFromRawType1(value: ByteArray, timeStamp: Long): EcgData {
            val ecgData = EcgData(timeStamp)
            var offset = 0
            while (offset < value.size) {
                val microVolts = (((value[offset]).toInt() and 0xFF) or (((value[offset + 1]).toInt() and 0x3F) shl 8)) and 0x3FFF
                val overSampling = (value[offset + 2].toInt() and 0x01) != 0
                val skinContactBit = ((value[offset + 2].toInt() and 0x06) shr 1).toByte()
                val contactImpedance = ((value[offset + 2].toInt() and 0x18) shr 3).toByte()
                offset += 3
                ecgData.ecgSamples.add(
                    EcgSample(
                        timeStamp = timeStamp,
                        microVolts = microVolts,
                        overSampling = overSampling,
                        skinContactBit = skinContactBit,
                        contactImpedance = contactImpedance
                    )
                )
            }
            return ecgData
        }

        private fun dataFromRawType2(value: ByteArray, timeStamp: Long): EcgData {
            val ecgData = EcgData(timeStamp)
            var offset = 0
            while (offset < value.size) {
                val microVolts = (value[offset].toInt() and 0xFF) or ((value[offset + 1].toInt() and 0xFF) shl 8) or ((value[offset + 2].toInt() and 0x03) shl 16) and 0x3FFFFF
                val ecgDataTag = ((value[offset + 2].toInt() and 0x1C) shr 2).toByte()
                val paceDataTag = ((value[offset + 2].toInt() and 0xE0) shr 5).toByte()
                offset += 3
                ecgData.ecgSamples.add(
                    EcgSample(
                        timeStamp = timeStamp,
                        microVolts = microVolts,
                        ecgDataTag = ecgDataTag,
                        paceDataTag = paceDataTag
                    )
                )
            }
            return ecgData
        }
    }
}