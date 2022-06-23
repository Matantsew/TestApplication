package com.example.testapplication

import org.xmlpull.v1.XmlPullParser

class ReceiptXmlParser {

    private val receipts: ArrayList<Receipt?> = ArrayList()

    fun getUsers(): ArrayList<Receipt?> {
        return receipts
    }

    fun parse(xpp: XmlPullParser): Boolean {

        var status = true
        var currentReceipt: Receipt? = null
        var inEntry = false
        var textValue: String? = ""

        try {
            var eventType = xpp.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {

                val tagName = xpp.name

                when (eventType) {

                    XmlPullParser.START_TAG -> {
                        if ("items".equals(tagName, ignoreCase = true)) {
                            inEntry = true
                            currentReceipt = Receipt()
                        }
                    }

                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> if (inEntry) {

                        if ("user".equals(tagName, ignoreCase = true)) {
                            receipts.add(currentReceipt)
                            inEntry = false
                        } else if ("name".equals(tagName, ignoreCase = true)) {
                            currentReceipt?.name = textValue
                        } else if ("age".equals(tagName, ignoreCase = true)) {
                            currentReceipt?.age = textValue
                        }
                    }
                    else -> {}
                }
                eventType = xpp.next()

            }
        } catch (e: Exception) {
            status = false
            e.printStackTrace()
        }

        return status
    }

}