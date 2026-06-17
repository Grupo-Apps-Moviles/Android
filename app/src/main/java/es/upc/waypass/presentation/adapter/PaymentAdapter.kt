package es.upc.waypass.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.upc.waypass.R
import es.upc.waypass.data.remote.ReservationResponse

class PaymentAdapter :
    RecyclerView.Adapter<
            PaymentAdapter.PaymentViewHolder>() {

    private var payments =
        listOf<ReservationResponse>()

    fun submitList(
        list: List<ReservationResponse>
    ) {
        payments = list
        notifyDataSetChanged()
    }

    inner class PaymentViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val txtAmount:
                TextView =
            itemView.findViewById(
                R.id.txtAmount
            )

        val txtCommission:
                TextView =
            itemView.findViewById(
                R.id.txtCommission
            )

        val txtDate:
                TextView =
            itemView.findViewById(
                R.id.txtDate
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_payment,
                parent,
                false
            )

        return PaymentViewHolder(view)
    }

    override fun getItemCount() =
        payments.size

    override fun onBindViewHolder(
        holder: PaymentViewHolder,
        position: Int
    ) {

        val payment =
            payments[position]

        holder.txtAmount.text =
            "Ganancia: S/ ${
                payment.driverEarnings
            }"

        holder.txtCommission.text =
            "Comisión: S/ ${
                payment.platformFee
            }"

        holder.txtDate.text =
            payment.createdAt
    }
}