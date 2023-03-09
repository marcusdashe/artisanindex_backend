package org.cstemp.artisanindex.programme

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.cstemp.artisanindex.artisan.Artisan
import java.util.*

@Entity
@Table(name = "programme")
class Programme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Size(min = 1, max = 70, message = "Title must be between 3 and 30 characters")
    @Column(name="title", nullable = false)
    var title: String? = null

    @Column(name = "description", nullable = true)
    var description: String? = null

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var created: Date = Date(System.currentTimeMillis())

    @ManyToOne(fetch = FetchType.EAGER)
    var artisan: Artisan? = null
}

