package com.example.cyrillic.util

object CyrillicToLatinMapping {

    fun getCyrillicToLatinMapping() = object : HashMap<String, String>() {
        init {
            put("Ә", "Á")
            put("ә", "á")
            put("Б", "B")
            put("б", "b")
            put("Д", "D")
            put("д", "d")
            put("Ф", "F")
            put("ф", "f")
            put("Ғ", "Ǵ")
            put("ғ", "ǵ")
            put("Г", "G")
            put("г", "g")
            put("Х", "H")
            put("х", "h")
            put("И", "I")
            put("и", "ı")
            put("Й", "I")
            put("й", "ı")
            put("Ж", "J")
            put("ж", "j")
            put("Л", "L")
            put("л", "l")
            put("к", "k")
            put("м", "m")
            put("Н", "N")
            put("н", "n")
            put("ц", "ts")
            put("Ң", "Ń")
            put("ң", "ń")
            put("Ө", "Ó")
            put("ө", "ó")
            put("П", "P")
            put("Ц", "Ts")
            put("п", "p")
            put("Қ", "Q")
            put("қ", "q")
            put("Р", "R")
            put("р", "r")
            put("Ш", "Sh")
            put("ш", "sh")
            put("С", "S")
            put("с", "s")
            put("т", "t")
            put("Ұ", "U")
            put("ұ", "u")
            put("Ү", "Ú")
            put("ү", "ú")
            put("В", "V")
            put("в", "v")
            put("Ы", "Y")
            put("ы", "y")
            put("У", "Ý")
            put("у", "ý")
            put("З", "Z")
            put("з", "z")
            put("Ч", "Ch")
            put("ч", "ch")
            put("Э", "E")
            put("э", "e")
            put("Щ", "")
            put("щ", "")
            put("ь", "")
            put("ъ", "")
            put("Я", "Ia")
            put("я", "ıa")
            put("Ю", "Iý")
            put("ю", "ıý")
        }
    }
}