import React from "react";

const getLoanDetails = (score) => {
  if (score >= 800) {
    return {
      tier: "Excellent",
      amount: "₹5,00,000",
      risk: "Very Low Risk",
      rate: "8.5%",
      action: "Pre-Approved",
      color: "bg-emerald-500",
      bgTone: "bg-emerald-50 dark:bg-emerald-950/30",
      borderTone: "border-emerald-200 dark:border-emerald-900",
      textTone: "text-emerald-700 dark:text-emerald-400",
      reasons: [
        "High consistency score (95%+)",
        "Exceptional earning stability",
        "Top-tier ratings across platforms"
      ],
      offsets: []
    };
  }
  if (score >= 700) {
    return {
      tier: "Low Risk",
      amount: "₹2,50,000",
      risk: "Low Risk",
      rate: "9.5%",
      action: "Eligible",
      color: "bg-blue-500",
      bgTone: "bg-blue-50 dark:bg-blue-950/30",
      borderTone: "border-blue-200 dark:border-blue-900",
      textTone: "text-blue-700 dark:text-blue-400",
      reasons: [
        "Stable earnings over 6 months",
        "High consistency score (85%)",
        "Strong rating history (4.8)"
      ],
      offsets: [
        "Minor platform diversity gap"
      ]
    };
  }
  if (score >= 600) {
    return {
      tier: "Medium",
      amount: "₹1,00,000",
      risk: "Moderate Risk",
      rate: "12.0%",
      action: "Limited Eligibility",
      color: "bg-amber-500",
      bgTone: "bg-amber-50 dark:bg-amber-950/30",
      borderTone: "border-amber-200 dark:border-amber-900",
      textTone: "text-amber-700 dark:text-amber-400",
      reasons: [
        "Moderate earnings stability",
        "Acceptable consistency (60%)"
      ],
      offsets: [
        "Recent gaps in activity",
        "Average ratings"
      ]
    };
  }
  return {
    tier: "High Risk",
    amount: "N/A",
    risk: "High Risk",
    rate: "N/A",
    action: "Manual Review",
    color: "bg-rose-500",
    bgTone: "bg-rose-50 dark:bg-rose-950/30",
    borderTone: "border-rose-200 dark:border-rose-900",
    textTone: "text-rose-700 dark:text-rose-400",
    reasons: [],
    offsets: [
        "Insufficient consistency data",
        "High earnings volatility",
        "Below average ratings"
    ]
  };
};

export default function LoanOffers({ score }) {
  const details = getLoanDetails(score);

  return (
    <div className={`border p-6 md:p-8 transition-colors duration-300 ${details.bgTone} ${details.borderTone}`}>
      <div className="flex flex-col gap-6 md:flex-row md:items-start md:justify-between">
        
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <span className={`h-2.5 w-2.5 rounded-full ${details.color}`}></span>
            <p className={`text-xs font-bold uppercase tracking-[0.2em] ${details.textTone}`}>
              Credit Eligibility
            </p>
          </div>
          <h3 className="mt-2 text-3xl font-black tracking-tight text-slate-900 dark:text-slate-100">
            {details.action}
          </h3>
          
          <div className="mt-6 space-y-4">
            {details.reasons.length > 0 && (
              <div>
                <p className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-2">Reason:</p>
                <ul className="space-y-1.5">
                  {details.reasons.map((reason, idx) => (
                    <li key={idx} className="flex items-start gap-2 text-sm text-slate-700 dark:text-slate-300">
                      <span className="text-emerald-500 font-bold">✔</span> {reason}
                    </li>
                  ))}
                </ul>
              </div>
            )}
            
            {details.offsets.length > 0 && (
              <div>
                <p className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-2">Risk Offset:</p>
                <ul className="space-y-1.5">
                  {details.offsets.map((offset, idx) => (
                    <li key={idx} className="flex items-start gap-2 text-sm text-slate-600 dark:text-slate-400">
                      <span className="text-rose-500 font-bold">−</span> {offset}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>

        <div className="flex shrink-0 flex-col gap-4 border-t border-slate-200 pt-6 dark:border-slate-800 md:border-t-0 md:border-l md:pt-0 md:pl-8">
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Max Loan Amount</p>
            <p className="mt-1 text-3xl font-black text-slate-900 dark:text-slate-100">{details.amount}</p>
          </div>
          {details.rate !== "N/A" && (
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Est. Interest Rate</p>
              <p className="mt-1 text-xl font-bold text-slate-700 dark:text-slate-300">{details.rate}</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
