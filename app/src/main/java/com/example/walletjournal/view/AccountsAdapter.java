package com.example.walletjournal.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.AccountType;

/**
 * Binds a dynamic list of Account rows into the Accounts screen's RecyclerView.
 */
public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {

    private final List<Account> accounts = new ArrayList<>();

    public void submitList(List<Account> newAccounts) {
        accounts.clear();
        if (newAccounts != null) {
            accounts.addAll(newAccounts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(accounts.get(position));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout iconCircle;
        private final TextView tvIconEmoji;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCircle = itemView.findViewById(R.id.item_icon_circle);
            tvIconEmoji = itemView.findViewById(R.id.item_icon_emoji);
            tvTitle = itemView.findViewById(R.id.item_title);
            tvSubtitle = itemView.findViewById(R.id.item_subtitle);
            tvAmount = itemView.findViewById(R.id.item_amount);
        }

        void bind(Account account) {
            tvTitle.setText(account.getTitle());
            tvSubtitle.setText(account.getSubtitle());

            long amount = account.getAmount();
            String sign = amount < 0 ? "-$" : "$";
            String formatted = sign + String.format(Locale.TAIWAN, "%,d", Math.abs(amount));
            tvAmount.setText(formatted);
            tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    amount < 0 ? R.color.accounts_amount_negative : R.color.accounts_text_primary));

            AccountType type = parseType(account.getType());
            tvIconEmoji.setText(emojiFor(type));
            iconCircle.setBackgroundResource(iconBackgroundFor(type));
        }

        private AccountType parseType(String raw) {
            if (raw != null) {
                for (AccountType type : AccountType.values()) {
                    if (type.name().equals(raw)) {
                        return type;
                    }
                }
            }
            return AccountType.CASH;
        }

        private String emojiFor(AccountType type) {
            switch (type) {
                case BANK:
                    return "🏦";
                case CREDIT_CARD:
                    return "💳";
                case CASH:
                default:
                    return "💵";
            }
        }

        private int iconBackgroundFor(AccountType type) {
            switch (type) {
                case BANK:
                    return R.drawable.bg_icon_circle_blue;
                case CREDIT_CARD:
                    return R.drawable.bg_icon_circle_orange;
                case CASH:
                default:
                    return R.drawable.bg_icon_circle_green;
            }
        }
    }
}
