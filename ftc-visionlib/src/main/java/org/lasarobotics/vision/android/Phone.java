package org.lasarobotics.vision.android;

import android.os.Build;

/**
 * Created by Josh on 12/23/2016.
 */
public class Phone {
    static PhoneType type;

    public static PhoneType getPhoneType () {
        if (type == null) {
            switch (Build.MODEL) {
                case "N9130":
                    type = PhoneType.ZTE_SPEED;
                    break;

                // https://en.wikipedia.org/wiki/Moto_G_(2nd_generation)#Model_variants
                case "XT1063":
                    type = PhoneType.MOTO_G2;
                    break;
                case "XT1064":
                    type = PhoneType.MOTO_G2;
                    break;
                case "XT1068":
                    type = PhoneType.MOTO_G2;
                    break;
                case "XT1069":
                    type = PhoneType.MOTO_G2;
                    break;
                case "XT1072":
                    type = PhoneType.MOTO_G2;
                    break;
                case "XT1077":
                    type = PhoneType.MOTO_G2;
                    break;
                case "XT1078":
                    type = PhoneType.MOTO_G2;
                    break;

                // https://en.wikipedia.org/wiki/Moto_G_(3rd_generation)#Variants
                case "XT1540":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1541":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1542":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1543":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1544":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1548":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1550":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1556":
                    type = PhoneType.MOTO_G3;
                    break;
                case "XT1557":
                    type = PhoneType.MOTO_G3;
                    break;

                // Sidenote: why the hell are there so many variants of phones this is ridiculous
                case "XT1607":
                    type = PhoneType.MOTO_G4_PLAY;
                    break;

                // https://en.wikipedia.org/wiki/Samsung_Galaxy_S5#Variants
                // sorry international people
                case "SM-G900T":
                    type = PhoneType.GALAXY_S5;
                    break;
                case "SM-G900T1":
                    type = PhoneType.GALAXY_S5;
                    break;
                case "SM-G900A":
                    type = PhoneType.GALAXY_S5;
                    break;
                case "SM-G900V":
                    type = PhoneType.GALAXY_S5;
                    break;
                case "SM-G900R4":
                    type = PhoneType.GALAXY_S5;
                    break;
                case "SM-G900P":
                    type = PhoneType.GALAXY_S5;
                    break;

                case "D280":
                    type = PhoneType.NEXUS_5;
                    break;
                case "D281":
                    type = PhoneType.NEXUS_5;
                    break;

                default:
                    type = PhoneType.UNKNOWN;
                    break;
            }
        }

        return type;
    }

    public enum PhoneType {
        // From what I can tell, most smartphone cameras have a focal length of about 30-35mm.

        // All of these assume a vertical orientation
        // TODO Add width for landscape
        ZTE_SPEED (30, 3.908), // Couldn't find this value in my research, should be easily figured out tho, https://www.wolframalpha.com/input/?i=a+%3D+4%2F3b,+sqrt(a%5E2%2Bb%5E2)+%3D+6.51282051273
        MOTO_G2 (27, 3.420), // IMX179 sensor
        MOTO_G3 (26, 3.520), // IMX214 https://www.wolframalpha.com/input/?i=a+%3D+4%2F3b,+sqrt(a%5E2+%2B+b%5E2)+%3D+5.867
        MOTO_G4_PLAY (30, 3.420), // Unknown sensor, just going to guess
        NEXUS_5 (30, 3.420), // This one's actually 30 mm
        GALAXY_S5 (31, 4.010),
        UNKNOWN (0, 0);

        // Both of these are measured in millimeters (yay metric)
        public final double focalLength;
        public final double sensorSize;

        PhoneType(double focalLength, double sensorSize) {
            this.focalLength = focalLength;
            this.sensorSize = sensorSize;
        }
    }
}
