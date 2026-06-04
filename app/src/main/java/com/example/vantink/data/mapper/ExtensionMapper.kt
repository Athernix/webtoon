package com.example.vantink.data.mapper

import com.example.vantink.data.local.entity.ActiveExtensionEntity
import com.example.vantink.data.remote.dto.ExtensionDto
import com.example.vantink.data.remote.dto.ExtensionSearchResultDto
import com.example.vantink.domain.model.Extension
import com.example.vantink.domain.model.Webtoon

fun ExtensionDto.toDomain(active: ActiveExtensionEntity? = null): Extension {
    return Extension(
        name = name,
        pkgName = pkgName,
        baseUrl = baseUrl,
        lang = lang,
        version = version,
        codeUrl = codeUrl,
        iconUrl = iconUrl,
        apkUrl = apkUrl,
        nsfw = nsfw,
        installedVersion = active?.version,
        isActive = active != null
    )
}

fun Extension.toActiveEntity(): ActiveExtensionEntity {
    return ActiveExtensionEntity(
        pkgName = pkgName,
        name = name,
        baseUrl = baseUrl,
        lang = lang,
        version = version,
        iconUrl = iconUrl
    )
}

fun ExtensionSearchResultDto.toWebtoon(): Webtoon {
    return Webtoon(
        id = "$sourcePkgName|$id",
        title = title,
        author = sourceName,
        thumbnailUrl = thumbnailUrl,
        status = "Remote"
    )
}
