const BOT_PRESENTATION = {
  travel_ai_guide: {
    eyebrow: "Lifestyle",
    accentClass: "bot-card travel",
    icon: "TR",
    title: "Travel",
    summary: "Plan trips, compare stays, review pricing, and move from discovery into booking.",
    highlights: ["Itinerary generation", "Hotel comparison", "Budget-aware booking help"]
  },
  education_helpful: {
    eyebrow: "Learning",
    accentClass: "bot-card education",
    icon: "ED",
    title: "Education",
    summary: "Explain concepts clearly, create study plans, and guide revision with practical follow-up prompts.",
    highlights: ["Concept explanation", "Study planning", "Quiz support"]
  }
};

function buildCard(chatbot) {
  return BOT_PRESENTATION[chatbot.chatbotCode] ?? {
    eyebrow: chatbot.domainName,
    accentClass: "bot-card generic",
    icon: chatbot.displayName.slice(0, 2).toUpperCase(),
    title: chatbot.displayName,
    summary: chatbot.description,
    highlights: chatbot.capabilities ?? []
  };
}

export function BotDashboardPage({ chatbots = [], onSelectBot }) {
  return (
    <div className="bot-dashboard-page">
      <section className="hero-panel">
        <div className="hero-copy">
          <p className="hero-eyebrow">AI Lifestyle Hub</p>
          <h1>Choose the assistant that matches the task.</h1>
          <p>
            Start from one central dashboard, then move into a dedicated workspace for travel,
            education, and any future bot you add.
          </p>
        </div>
        <div className="hero-sidekick">
          <span>Available bots</span>
          <strong>{chatbots.length}</strong>
          <p>Each tile opens its own purpose-built page with the right assistant context.</p>
        </div>
      </section>

      <section className="bot-grid">
        {chatbots.map((chatbot) => {
          const card = buildCard(chatbot);
          return (
            <article key={chatbot.chatbotCode} className={card.accentClass}>
              <div className="bot-card-header">
                <span className="bot-card-eyebrow">{card.eyebrow}</span>
                <div className="bot-card-icon">{card.icon}</div>
              </div>
              <div className="bot-card-body">
                <h2>{card.title}</h2>
                <p>{card.summary}</p>
              </div>
              <ul className="bot-card-highlights">
                {card.highlights.slice(0, 3).map((highlight) => (
                  <li key={highlight}>{highlight}</li>
                ))}
              </ul>
              <button type="button" className="bot-card-action" onClick={() => onSelectBot(chatbot.chatbotCode)}>
                Open {card.title}
              </button>
            </article>
          );
        })}
      </section>
    </div>
  );
}
