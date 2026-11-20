package com.example.S2435823HS;


import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/***
 * pull parsr class
 */

public class PullParser {

    public List<PlannedEvents> parse(String xml, String source)
            throws Exception {

        List<PlannedEvents> events = new ArrayList<>();

        XmlPullParser xpp = Xml.newPullParser();
        xpp.setFeature(
                XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        xpp.setInput(new StringReader(xml));

        boolean insideSituation = false;
        boolean insideComment = false;
        boolean insideIlc = false;
        boolean insideIlcDescriptor = false;
        boolean insideName = false;
        boolean insideNameDescriptor = false;
        boolean insideTo = false;
        boolean insideFrom = false;

        String ilcValue = null;
        String ilcType = null;

        String nameValue = null;
        String nameType = null;

        PlannedEvents aSituation = null;

        int eventType = xpp.getEventType();
        /**
         * bellow traverses a situation to find and extract the data required
         */

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {

                String tag = xpp.getName();

                if (tag.equalsIgnoreCase("situation")) {

                    insideSituation = true;
                    aSituation = new PlannedEvents();
                    aSituation.setSource(source);

                } else if (insideSituation &&
                        tag.equalsIgnoreCase("generalPublicComment")) {

                    insideComment = true;

                } else if (insideSituation &&
                        tag.equalsIgnoreCase("to")) {

                    insideTo = true;

                } else if (insideSituation &&
                        tag.equalsIgnoreCase("from")) {

                    insideFrom = true;

                } else if (tag.equalsIgnoreCase("ilc")
                        && insideSituation) {

                    insideIlc = true;
                    ilcValue = null;
                    ilcType = null;

                } else if (tag.equalsIgnoreCase("descriptor")
                        && insideIlc) {

                    insideIlcDescriptor = true;

                } else if (tag.equalsIgnoreCase("value")
                        && insideIlcDescriptor) {

                    ilcValue = xpp.nextText().trim();

                } else if (tag.equalsIgnoreCase(
                        "tpegIlcPointDescriptorType")
                        && insideIlc) {

                    ilcType = xpp.nextText().trim();

                } else if (tag.equalsIgnoreCase("name")
                        && insideSituation) {

                    insideName = true;
                    nameValue = null;
                    nameType = null;

                } else if (tag.equalsIgnoreCase("descriptor")
                        && insideName) {

                    insideNameDescriptor = true;

                } else if (tag.equalsIgnoreCase("value")
                        && insideNameDescriptor) {

                    nameValue = xpp.nextText().trim();

                } else if (tag.equalsIgnoreCase(
                        "tpegOtherPointDescriptorType")
                        && insideName) {

                    nameType = xpp.nextText().trim();

                } else if (tag.equalsIgnoreCase("value")
                        && insideComment) {

                    String comment = xpp.nextText();

                    if (aSituation != null) {
                        aSituation.setComment(comment);
                    }

                } else if (tag.equalsIgnoreCase("delaysType")
                        && insideSituation) {

                    String delayType = xpp.nextText().trim();

                    if (aSituation != null) {
                        aSituation.setDelayType(delayType);

                    }


                } else if (tag.equalsIgnoreCase(
                        "roadOrCarriagewayOrLaneManagementType")) {

                    String type = xpp.nextText().trim();

                    if (aSituation != null) {
                        aSituation.setType(type);
                    }

                } else if (tag.equalsIgnoreCase("latitude")) {

                    String value = xpp.nextText().trim();

                    try {
                        double latitude = Double.parseDouble(value);

                        if (aSituation != null && insideTo) {
                            aSituation.setToLatitude(latitude);

                        } else if (aSituation != null && insideFrom) {
                            aSituation.setFromLatitude(latitude);
                        }

                    } catch (NumberFormatException e) {
                        // Ignore invalid latitude values.
                    }

                } else if (tag.equalsIgnoreCase("longitude")) {

                    String value = xpp.nextText().trim();

                    try {
                        double longitude = Double.parseDouble(value);

                        if (aSituation != null && insideTo) {
                            aSituation.setToLongitude(longitude);

                        } else if (aSituation != null && insideFrom) {
                            aSituation.setFromLongitude(longitude);
                        }

                    } catch (NumberFormatException e) {
                        // Ignore invalid longitude values.
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {

                String tag = xpp.getName();

                if (tag.equalsIgnoreCase("generalPublicComment")) {

                    insideComment = false;

                } else if (tag.equalsIgnoreCase("descriptor")) {

                    insideIlcDescriptor = false;
                    insideNameDescriptor = false;

                } else if (tag.equalsIgnoreCase("ilc")) {

                    if (aSituation != null
                            && "tpegIlcName1".equalsIgnoreCase(ilcType)
                            && ilcValue != null
                            && aSituation.getRoadName() == null) {

                        aSituation.setRoadName(ilcValue);
                    }

                    insideIlc = false;
                    insideIlcDescriptor = false;
                    ilcValue = null;
                    ilcType = null;

                } else if (tag.equalsIgnoreCase("name")) {

                    if (aSituation != null
                            && nameValue != null
                            && aSituation.getRoadName() == null
                            && "linkName".equalsIgnoreCase(nameType)) {

                        aSituation.setRoadName(nameValue);
                    }

                    insideName = false;
                    insideNameDescriptor = false;
                    nameValue = null;
                    nameType = null;

                } else if (tag.equalsIgnoreCase("to")) {

                    insideTo = false;

                } else if (tag.equalsIgnoreCase("from")) {

                    insideFrom = false;

                } else if (tag.equalsIgnoreCase("situation")) {

                    // Add the event to the results.
                    if (aSituation != null) {
                        events.add(aSituation);
                    }

                    // Reset all situation-specific values.
                    aSituation = null;
                    insideSituation = false;
                    insideComment = false;
                    insideIlc = false;
                    insideIlcDescriptor = false;
                    insideName = false;
                    insideNameDescriptor = false;
                    insideTo = false;
                    insideFrom = false;

                    ilcValue = null;
                    ilcType = null;
                    nameValue = null;
                    nameType = null;
                }
            }

            eventType = xpp.next();
        }

        return events;
    }
}
