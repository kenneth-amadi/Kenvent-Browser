package com.kixfobby.project.kenventbrowser.ui.main

import android.graphics.Bitmap
import java.io.Serializable

data class Tabs(
    var title: String?,
    var url: String?,
    var preview: ByteArray?,
    var favicon: ByteArray?,
    var position: Int?
) :
    Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tabs

        if (title != other.title) return false
        if (url != other.url) return false
        if (preview != null) {
            if (other.preview == null) return false
            if (!preview.contentEquals(other.preview)) return false
        } else if (other.preview != null) return false
        if (favicon != null) {
            if (other.favicon == null) return false
            if (!favicon.contentEquals(other.favicon)) return false
        } else if (other.favicon != null) return false
        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (preview?.contentHashCode() ?: 0)
        result = 31 * result + (favicon?.contentHashCode() ?: 0)
        result = 31 * result + (position ?: 0)
        return result
    }
}
