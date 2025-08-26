package gr.eduinvoice.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
	tableName = "payment_batch_master",
	indices = [Index(value = ["studentId"]), Index(value = ["batchDate"])]
)
data class PaymentBatchMaster(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val ownerId: Long = 0,
	val studentId: Long? = null,
	val batchDate: String,
	val notes: String? = null,
	@ColumnInfo(defaultValue = "0")
	val isArchived: Boolean = false,
	@ColumnInfo(defaultValue = "0")
	val lastModified: Long = System.currentTimeMillis()
)
