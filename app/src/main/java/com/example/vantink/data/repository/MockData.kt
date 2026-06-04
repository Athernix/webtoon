package com.example.vantink.data.repository

import com.example.vantink.domain.model.Chapter
import com.example.vantink.domain.model.ChapterSummary
import com.example.vantink.domain.model.Webtoon

object MockData {
    val webtoons = listOf(
        Webtoon(
            id = "1",
            title = "Solo Leveling",
            author = "Chugong",
            artist = "DUBU",
            description = "Ten years ago, after 'the Gate' that connected the real world with the monster world opened, some of the ordinary, everyday people received the power to hunt monsters within the Gate. They are known as 'Hunters'.",
            thumbnailUrl = "https://static.wikia.nocookie.net/solo-leveling/images/e/e8/Solo_Leveling_Webtoon.png",
            status = "Completed",
            genres = listOf("Action", "Adventure", "Fantasy"),
            chapters = listOf(
                ChapterSummary("101", "Chapter 1", 1.0f, "2024-01-01"),
                ChapterSummary("102", "Chapter 2", 2.0f, "2024-01-02"),
                ChapterSummary("103", "Chapter 3", 3.0f, "2024-01-03")
            )
        ),
        Webtoon(
            id = "2",
            title = "Tower of God",
            author = "SIU",
            description = "What do you desire? Money and wealth? Honor and pride? Authority and power? Revenge? Or something that transcends them all? Whatever you desire—it's here.",
            thumbnailUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/0/09/Tower_of_God_Manhwa_Volume_1.jpg/220px-Tower_of_God_Manhwa_Volume_1.jpg",
            status = "Ongoing",
            genres = listOf("Action", "Adventure", "Drama", "Fantasy"),
            chapters = listOf(
                ChapterSummary("201", "Chapter 1", 1.0f, "2024-01-01"),
                ChapterSummary("202", "Chapter 2", 2.0f, "2024-01-02")
            )
        ),
        Webtoon(
            id = "3",
            title = "The Beginning After The End",
            author = "TurtleMe",
            description = "King Grey has unrivaled strength, riches, and prestige in a world governed by martial ability. However, solitude lingers closely behind those with great power.",
            thumbnailUrl = "https://m.media-amazon.com/images/I/71Xm3T2o2LL._AC_UF1000,1000_QL80_.jpg",
            status = "Ongoing",
            genres = listOf("Action", "Adventure", "Fantasy", "Reincarnation"),
            chapters = listOf(
                ChapterSummary("301", "Chapter 1", 1.0f, "2024-01-01")
            )
        )
    )

    fun getChapter(chapterId: String): Chapter {
        val webtoonId = when {
            chapterId.startsWith("1") -> "1"
            chapterId.startsWith("2") -> "2"
            else -> "3"
        }
        val title = "Chapter ${chapterId.last()}"
        val number = chapterId.last().toString().toFloat()
        
        return Chapter(
            id = chapterId,
            webtoonId = webtoonId,
            title = title,
            number = number,
            pages = listOf(
                "https://picsum.photos/seed/p1/800/1200",
                "https://picsum.photos/seed/p2/800/1200",
                "https://picsum.photos/seed/p3/800/1200",
                "https://picsum.photos/seed/p4/800/1200",
                "https://picsum.photos/seed/p5/800/1200"
            ),
            uploadDate = "2024-01-01"
        )
    }
}
