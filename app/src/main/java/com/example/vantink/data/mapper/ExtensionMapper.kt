package com.example.vantink.data.mapper

import com.example.vantink.data.local.entity.ActiveExtensionEntity
import com.example.vantink.domain.model.Extension

fun Extension.toActiveEntity(): ActiveExtensionEntity {
    return ActiveExtensionEntity(
        pkgName = pkgName,
        name = name,
        baseUrl = baseUrl,
        lang = lang,
        version = version,
        iconUrl = iconUrl,
        sourceType = sourceType,
        isMetaProvider = isMetaProvider,
        isDirectory = isDirectory
    )
}
