package com.example.bubtrack.domain.article

data class ArticleModel(
    val id: Int,
    val title: String,
    val content: String,
    val date: String,
    val source: String,
    val imageUrl: String,
    val category: String,
    val readTime: Int
)

val dummyArticle = listOf(
    ArticleModel(
        id = 1,
        title = "Tips Menenangkan Bayi yang Sering Menangis",
        content = "Bayi sering menangis karena lapar, lelah, atau butuh perhatian. Orang tua perlu memahami sinyal ini agar bisa merespons dengan tepat.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 3
    ),
    ArticleModel(
        id = 2,
        title = "Manfaat Skin to Skin Contact pada Bayi Baru Lahir",
        content = "Kontak kulit antara ibu dan bayi terbukti membantu menstabilkan suhu tubuh bayi dan mempererat ikatan emosional.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 4
    ),
    ArticleModel(
        id = 3,
        title = "Pola Tidur Bayi: Apa yang Normal?",
        content = "Bayi baru lahir biasanya tidur 14–17 jam per hari. Namun, pola tidur ini sering terputus-putus dan wajar terjadi.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 5
    ),
    ArticleModel(
        id = 4,
        title = "Kapan Bayi Boleh Diberikan MPASI?",
        content = "Menurut WHO, bayi mulai bisa diberikan makanan pendamping ASI saat berusia 6 bulan dengan tekstur lembut.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 4
    ),
    ArticleModel(
        id = 5,
        title = "Cara Aman Menggendong Bayi yang Benar",
        content = "Menggendong bayi harus memperhatikan posisi kepala dan leher agar tetap aman dan nyaman bagi bayi.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 3
    ),
    ArticleModel(
        id = 6,
        title = "Mengatasi Ruam Popok pada Bayi",
        content = "Ruam popok dapat dihindari dengan menjaga area tetap kering, sering mengganti popok, dan menggunakan krim pelindung.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 4
    ),
    ArticleModel(
        id = 7,
        title = "Tanda Bayi Sedang Tumbuh Gigi",
        content = "Bayi biasanya rewel, suka menggigit benda, dan mengeluarkan banyak air liur saat tumbuh gigi pertama kali.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 3
    ),
    ArticleModel(
        id = 8,
        title = "Perlukah Bayi Minum Air Putih?",
        content = "Bayi di bawah 6 bulan tidak perlu diberikan air putih karena kebutuhan cairannya sudah terpenuhi dari ASI atau susu formula.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 5
    ),
    ArticleModel(
        id = 9,
        title = "Stimulasi Motorik untuk Bayi Usia 3 Bulan",
        content = "Memberikan mainan warna-warni dan tummy time dapat membantu perkembangan motorik bayi sejak dini.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 4
    ),
    ArticleModel(
        id = 10,
        title = "Mitos dan Fakta Tentang Bayi",
        content = "Banyak mitos beredar tentang perawatan bayi. Penting bagi orang tua untuk memisahkan antara mitos dan fakta ilmiah.",
        date = "5 Agustus 2025",
        source = "Detik.com",
        imageUrl = "https://asimor.co.id/storage/app/uploads/public/689/9a0/ec1/6899a0ec171dd378956695.jpg",
        category = "Parenting",
        readTime = 6
    )
)

