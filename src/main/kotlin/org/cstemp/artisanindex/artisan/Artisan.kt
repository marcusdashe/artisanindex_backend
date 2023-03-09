package org.cstemp.artisanindex.artisan

import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.cstemp.artisanindex.programme.Programme
import java.util.*

@Entity
@Table(name = "artisan")
class Artisan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Size(min = 3, max = 70, message = "Name must be between 3 and 30 characters")
    @Column(name="full_name", nullable = false)
    var fullName: String? = null

    @Size(min = 3, max = 40, message = "Trade must be between 3 and 40 characters")
    @Column(name="trade", nullable = false)
    var trade: String? = null

    @Size(min = 9, max = 13, message = " Phone number must phone be between 9 and 13 characters")
    @Column(name="phone_Number", nullable = false, unique = true)
    var phoneNumber: String? = null

    @Column(name="gender", nullable = false)
    @Enumerated(EnumType.STRING)
    var gender: AppConstants.Gender = AppConstants.Gender.OTHER

    @Size(min = 2, max = 35, message = "City must be between 9 and 35 characters")
    @Column(name="city", nullable = false)
    var city: String? = null

    @Size(min = 2, max = 35, message = "State must be between 9 and 35 characters")
    @Column(name="state", nullable = false)
    var state: String? = null

    @OneToMany(mappedBy = "artisan", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY, orphanRemoval = true)
    private val programmes: MutableSet<Programme> = mutableSetOf<Programme>()

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var created: Date = Date(System.currentTimeMillis())

    fun addProgramme(programme: Programme) {
        programmes.add(programme)
        programme.artisan = this
    }
}

//
//    public void addProgramme(Programme programme) {
//        programmes.add(programme);
//        programme.setArtisan(this);
//    }
//
//    public void removeProgramme(Programme programme) {
//        programmes.remove(programme);
//        programme.setArtisan(null);
//    }
//
//}
