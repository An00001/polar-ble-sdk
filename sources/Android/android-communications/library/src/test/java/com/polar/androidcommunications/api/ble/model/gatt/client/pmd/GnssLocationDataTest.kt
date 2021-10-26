package com.polar.androidcommunications.api.ble.model.gatt.client.pmd

import com.polar.androidcommunications.api.ble.model.gatt.client.pmd.model.GnssLocationData
import org.junit.Assert
import org.junit.Test
import java.lang.Double.longBitsToDouble
import java.lang.Float.intBitsToFloat

class GnssLocationDataTest {

    @Test
    fun `process location data type 0`() {
        // Arrange
        // HEX: 00 00 00 00 4C E2 81 42 00 00 00 00 FB 8F CB 41 E5 07 0A 01 00 14 8D 03 FF FF FF 0F E1 FA C9 42 CD CC CC 3D 48 61 8B 42 00 00 02 00 03 8D FF FF 01 FF FF
        // index    type                                data
        // 0..7    Latitude                             00 00 00 00 4C E2 81 42 (0x4281E24C)
        // 8..15   Longitude                            00 00 00 00 FB 8F CB 41 (0x41cb8ffb)
        // 16..19  Date                                 E5 07 0A 01 (year = 2021, month = 10, date = 1)
        // 20..23  Time                                 00 2C 47 0C (0x0C472C00 => hours = 11 , minutes = 14, seconds = 34, milliseconds = 000 trusted = true)
        // 24..27  Cumulative distance                  FF FF FF 0F (0x0FFFFFFF = 268435455)
        // 28..31  Speed                                E1 FA C9 42 (0x42C9FAE1 => 100.99km/h)
        // 32..35  Acceleration Speed                   CD CC CC 3D (0x3DCCCCCD => 0.1)
        // 36..39  Coordinate Speed                     48 61 8B 42 (0x428B6148 => 69.69)
        // 40..43  Acceleration Speed Factor            00 00 02 00 (0x00000200)
        // 44..45  Course                               03 8D (0x8CA0 = 360.99 degrees )
        // 46..47  Knots speed                          FF FF (0xFFFF = 655.35 )
        // 48      Fix                                  01 (true)
        // 49      Speed flag                           FF
        // 50      Fusion state                         FF
        val expectedSamplesSize = 1
        val expectedTimeStamp = 946833049921875000L
        val latitude = longBitsToDouble(0x4281e24c00000000)
        val longitude = longBitsToDouble(0x41cb8ffb00000000)
        val date = "2021-10-01T11:14:34.000"
        val cumulativeDistance = 26843545.5
        val speed = intBitsToFloat(0x42C9FAE1)
        val accelerationSpeed = intBitsToFloat(0x3DCCCCCD)
        val coordinateSpeed = intBitsToFloat(0x428B6148)
        val accelerationSpeedFactor = intBitsToFloat(0x00000200)
        val course = 360.99f
        val gpsChipSpeed = 655.35f
        val speedFlag = -1
        val fusionState = 0xFFu

        val measurementFrame = byteArrayOf(
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x4C.toByte(), 0xE2.toByte(), 0x81.toByte(), 0x42.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0xFB.toByte(), 0x8F.toByte(), 0xCB.toByte(), 0x41.toByte(),
            0xE5.toByte(), 0x07.toByte(), 0x0A.toByte(), 0x01.toByte(),
            0x00.toByte(), 0x2C.toByte(), 0x47.toByte(), 0x0C.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x0F.toByte(), 0xE1.toByte(), 0xFA.toByte(), 0xC9.toByte(), 0x42.toByte(),
            0xCD.toByte(), 0xCC.toByte(), 0xCC.toByte(), 0x3D.toByte(), 0x48.toByte(), 0x61.toByte(), 0x8B.toByte(), 0x42.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x00.toByte(), 0x03.toByte(), 0x8D.toByte(), 0xFF.toByte(), 0xFF.toByte(),
            0x01.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )

        // Act
        val gnssData = GnssLocationData.parseDataFromDataFrame(isCompressed = false, frameType = BlePMDClient.PmdDataFrameType.TYPE_0, frame = measurementFrame, factor = 1.0f, timeStamp = expectedTimeStamp)

        Assert.assertEquals(expectedTimeStamp, gnssData.timeStamp)
        Assert.assertEquals(expectedSamplesSize, gnssData.gnssLocationDataSamples.size)
        Assert.assertTrue(gnssData.gnssLocationDataSamples[0] is GnssLocationData.GnssCoordinateSample)
        Assert.assertEquals(latitude, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).latitude, 0.00001)
        Assert.assertEquals(longitude, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).longitude, 0.00001)
        Assert.assertEquals(date, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).date)
        Assert.assertEquals(cumulativeDistance, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).cumulativeDistance, 0.00001)
        Assert.assertEquals(speed, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).speed)
        Assert.assertEquals(accelerationSpeed, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).usedAccelerationSpeed)
        Assert.assertEquals(coordinateSpeed, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).coordinateSpeed)
        Assert.assertEquals(accelerationSpeedFactor, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).accelerationSpeedFactor, 0.00001f)
        Assert.assertEquals(course, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).course)
        Assert.assertEquals(gpsChipSpeed, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).gpsChipSpeed)
        Assert.assertTrue((gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).fix)
        Assert.assertEquals(speedFlag, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).speedFlag)
        Assert.assertEquals(fusionState, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssCoordinateSample).fusionState)
    }

    @Test
    fun `process location data type 1`() {
        // Arrange
        // HEX: F5 07 00 80 FF 07
        // index    type                                data
        // 0..1:    Dilution                            F5 07 (0x7F5 = 20.37)
        // 2..3:    Altitude                            00 80 (0xFFFF = -32768)
        // 4:       Number of satellites                FF
        // 5:       Fix                                 07
        val expectedSamplesSize = 1
        val expectedTimeStamp = 946784976788085937L
        val dilution = 20.37f
        val altitude = -32768
        val numberOfSatellites = 255u

        val measurementFrame = byteArrayOf(0xF5.toByte(), 0x07.toByte(), 0x00.toByte(), 0x80.toByte(), 0xFF.toByte(), 0x07.toByte())

        // Act
        val gnssData = GnssLocationData.parseDataFromDataFrame(isCompressed = false, frameType = BlePMDClient.PmdDataFrameType.TYPE_1, frame = measurementFrame, factor = 1.0f, timeStamp = expectedTimeStamp)

        // Assert
        Assert.assertEquals(expectedTimeStamp, gnssData.timeStamp)
        Assert.assertEquals(expectedSamplesSize, gnssData.gnssLocationDataSamples.size)
        Assert.assertEquals(dilution, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssSatelliteDilutionSample).dilution)
        Assert.assertEquals(altitude, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssSatelliteDilutionSample).altitude)
        Assert.assertEquals(numberOfSatellites, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssSatelliteDilutionSample).numberOfSatellites)
        Assert.assertTrue((gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssSatelliteDilutionSample).fix)
    }

    @Test
    fun `process location data type 2`() {
        // Arrange
        // HEX: 00 01 02 03 04 05 06 07 FF EF 80 00 01 02 03 04
        // index    type                                data
        // 0..7:    Seen satellite summary              00 01 02 03 04 05 06 07
        // 8..15:   Used satellite summary              FF EF 80 00 01 02 03 04

        val expectedSamplesSize = 1
        val expectedTimeStamp = 946784976788085937L
        val seenSatelliteSummary = GnssLocationData.GnssSatelliteSummary(
            gpsSat = 0u,
            gpsMaxSnr = 1u,
            glonassSat = 2u,
            glonassMaxSnr = 3u,
            sbasSat = 4u,
            sbasMaxSnr = 5u,
            snrTop5Avg = 6u,
            sbasSnrTop5Avg = 7u
        )
        val usedSatelliteSummary = GnssLocationData.GnssSatelliteSummary(
            gpsSat = 0xFFu,
            gpsMaxSnr = 0xEFu,
            glonassSat = 0x80u,
            glonassMaxSnr = 0x00u,
            sbasSat = 1u,
            sbasMaxSnr = 2u,
            snrTop5Avg = 3u,
            sbasSnrTop5Avg = 4u
        )

        val measurementFrame = byteArrayOf(
            0x00.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte(), 0x07.toByte(),
            0xFF.toByte(), 0xEF.toByte(), 0x80.toByte(), 0x00.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(),
        )

        // Act
        val gnssData = GnssLocationData.parseDataFromDataFrame(isCompressed = false, frameType = BlePMDClient.PmdDataFrameType.TYPE_2, frame = measurementFrame, factor = 1.0f, timeStamp = expectedTimeStamp)

        // Assert
        Assert.assertEquals(expectedTimeStamp, gnssData.timeStamp)
        Assert.assertEquals(expectedSamplesSize, gnssData.gnssLocationDataSamples.size)
        Assert.assertEquals(seenSatelliteSummary, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssSatelliteSummarySample).seenGnssSatelliteSummary)
        Assert.assertEquals(usedSatelliteSummary, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssSatelliteSummarySample).usedGnssSatelliteSummary)
    }

    @Test
    fun `process location data type 3`() {
        // Arrange
        // HEX: E8 03 00 00 1C 00 00 1A 80 47 50 41 41 4D 2C 41 2C 41 2C 30 2E 31 30 2C 4E 2C 57 50 54 4E 4D 45 2A 33 32
        // index    type                                data
        // 0..3:    Measurement period                  00 00 1C 00 (0x001C0000 = 1835008)
        // 4..5:    NMEA Message length                 1A 00  (0x1A = 26)
        // 6:       Status flags                        80
        // 7..      NMEA message                        47 50 41 41 4D 2C 41 2C 41 2C 30 2E 31 30 2C 4E 2C 57 50 54 4E 4D 45 2A 33 32

        val expectedSamplesSize = 1
        val expectedTimeStamp = 946871122724609375L
        val measurementPeriod = 1835008u
        val nmeaMessageLength = 26u
        val statusFlags = 0x80u.toUByte()
        val nmeaMessage = "GPAAM,A,A,0.10,N,WPTNME*32"
        val measurementFrame = byteArrayOf(
            0x00.toByte(), 0x00.toByte(), 0x1C.toByte(), 0x00.toByte(), 0x1A.toByte(), 0x00.toByte(), 0x80.toByte(), 0x47.toByte(),
            0x50.toByte(), 0x41.toByte(), 0x41.toByte(), 0x4D.toByte(), 0x2C.toByte(), 0x41.toByte(), 0x2C.toByte(), 0x41.toByte(),
            0x2C.toByte(), 0x30.toByte(), 0x2E.toByte(), 0x31.toByte(), 0x30.toByte(), 0x2C.toByte(), 0x4E.toByte(), 0x2C.toByte(),
            0x57.toByte(), 0x50.toByte(), 0x54.toByte(), 0x4E.toByte(), 0x4D.toByte(), 0x45.toByte(), 0x2A.toByte(), 0x33.toByte(),
            0x32.toByte()
        )

        // Act
        val gnssData = GnssLocationData.parseDataFromDataFrame(isCompressed = false, frameType = BlePMDClient.PmdDataFrameType.TYPE_3, frame = measurementFrame, factor = 1.0f, timeStamp = expectedTimeStamp)

        // Assert
        Assert.assertEquals(expectedTimeStamp, gnssData.timeStamp)
        Assert.assertEquals(expectedSamplesSize, gnssData.gnssLocationDataSamples.size)
        Assert.assertEquals(measurementPeriod, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssGpsNMEASample).measurementPeriod)
        Assert.assertEquals(nmeaMessageLength, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssGpsNMEASample).messageLength)
        Assert.assertEquals(statusFlags, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssGpsNMEASample).statusFlags)
        Assert.assertEquals(nmeaMessage, (gnssData.gnssLocationDataSamples[0] as GnssLocationData.GnssGpsNMEASample).nmeaMessage)
    }


}