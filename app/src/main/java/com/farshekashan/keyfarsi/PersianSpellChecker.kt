package com.farshekashan.keyfarsi

/** Offline Persian typo ranker. Dictionary candidates are loaded from assets and user vocabulary. */
internal object PersianSpellChecker {
    data class Candidate(val word: String, val frequency: Int = 1, val contextFrequency: Int = 0, val trigramFrequency: Int = 0)
    data class Match(val word: String, val score: Int, val distance: Int, val frequency: Int, val contextFrequency: Int, val trigramFrequency: Int)

    fun normalize(input: String): String = input.trim()
        .replace('ي', 'ی').replace('ك', 'ک').replace('ۀ', 'ه').replace('ة', 'ه')
        .replace("ـ", "").replace("\u200c", "").replace("\u200f", "").replace("\u200e", "").lowercase()

    private val rows = listOf("ضصثقفغعهخحجچ", "شسیبلاتنمکگ", "ظطزرژذدپو")
    private val neighbors: Map<Char, Set<Char>> by lazy {
        buildMap {
            rows.forEachIndexed { r, row -> row.forEachIndexed { c, ch ->
                val near = linkedSetOf<Char>()
                for (rr in (r - 1)..(r + 1)) for (cc in (c - 1)..(c + 1))
                    if (rr in rows.indices && cc in rows[rr].indices && (rr != r || cc != c)) near += rows[rr][cc]
                put(ch, near)
            } }
        }
    }

    // Weighted Damerau/OSA distance: adjacent keyboard mistakes and transpositions cost less.
    private fun distance(a: String, b: String, limit: Int): Int {
        if (kotlin.math.abs(a.length - b.length) * 2 > limit) return limit + 1
        val d = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) d[i][0] = i * 2
        for (j in 0..b.length) d[0][j] = j * 2
        for (i in 1..a.length) {
            var rowMin = Int.MAX_VALUE
            for (j in 1..b.length) {
                val x = a[i - 1]; val y = b[j - 1]
                val sub = when { x == y -> 0; neighbors[x]?.contains(y) == true -> 1; else -> 2 }
                var v = minOf(d[i - 1][j] + 2, d[i][j - 1] + 2, d[i - 1][j - 1] + sub)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) v = minOf(v, d[i - 2][j - 2] + 1)
                d[i][j] = v; rowMin = minOf(rowMin, v)
            }
            if (rowMin > limit) return limit + 1
        }
        return d[a.length][b.length]
    }

    fun rank(input: String, candidates: Sequence<Candidate>): List<Match> {
        val typed = normalize(input)
        if (typed.length < 3 || typed.any { it !in '\u0600'..'\u06FF' }) return emptyList()
        val maxCost = if (typed.length <= 4) 2 else 3
        return candidates.mapNotNull { c ->
            val normalized = normalize(c.word)
            if (normalized == typed || normalized.length < 2) return@mapNotNull null
            val dist = distance(typed, normalized, maxCost)
            if (dist > maxCost) return@mapNotNull null
            // Context has a stronger influence than raw dictionary frequency. This is
            // still a local statistical model (not a cloud/neural language model).
            val score = dist * 1000 - (kotlin.math.ln(1.0 + c.frequency.coerceAtLeast(0)) * 22).toInt() -
                (kotlin.math.ln(1.0 + c.contextFrequency.coerceAtLeast(0)) * 115).toInt() -
                (kotlin.math.ln(1.0 + c.trigramFrequency.coerceAtLeast(0)) * 260).toInt()
            Match(c.word, score, dist, c.frequency, c.contextFrequency, c.trigramFrequency)
        }.sortedWith(compareBy<Match> { it.score }.thenBy { it.word.length }).toList()
    }

    /** Only auto-correct a single low-cost winner with a clear margin. */
    fun confidentCorrection(
        input: String, candidates: Sequence<Candidate>, rankedOverride: List<Match>? = null
    ): String? {
        val ranked = (rankedOverride ?: rank(input, candidates)).take(2)
        if (ranked.isEmpty()) return null
        val first = ranked[0]
        // A one-cost typo is eligible when the winner is clearly separated.
        // A two-cost typo is eligible only when context has repeatedly supported it
        // or the user has personally used the candidate often. Otherwise it stays a suggestion.
        if (first.distance > 2) return null
        if (first.distance == 2 && first.contextFrequency < 2 && first.trigramFrequency < 2 && first.frequency < 20) return null
        val requiredMargin = if (first.distance == 1) 260 else 450
        if (ranked.size > 1 && ranked[1].score - first.score < requiredMargin) return null
        return first.word
    }
}
