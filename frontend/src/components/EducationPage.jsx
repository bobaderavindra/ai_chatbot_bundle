function formatChatTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return new Intl.DateTimeFormat("en-SG", {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  }).format(date);
}

const studyTopics = [
  "Explain recursion with a simple example",
  "Create a 7-day study plan for algebra",
  "Quiz me on world history",
  "Summarize photosynthesis in plain English"
];

export function EducationPage({
  activeChatbot,
  activeSession,
  chatInput,
  chatResponse,
  chatSessions = [],
  error,
  isChatLoading,
  isSendingMessage,
  onBack,
  onChatInputChange,
  onNewSession,
  onSelectSession,
  onSendMessage
}) {
  const suggestedActions = chatResponse?.suggestedActions ?? [];

  return (
    <div className="education-page">
      <header className="section-topbar">
        <button type="button" className="back-button" onClick={onBack}>Back to dashboard</button>
        <div className="section-title-block">
          <p className="hero-eyebrow">Education Workspace</p>
          <h1>{activeChatbot?.displayName ?? "EducationHelpful"}</h1>
          <p>Structured learning help with focused study prompts, revision support, and session memory.</p>
        </div>
        <button type="button" className="secondary-button" onClick={onNewSession}>New study chat</button>
      </header>

      {error ? <div className="status-banner error">{error}</div> : null}

      <main className="education-layout">
        <section className="education-overview">
          <article className="education-panel spotlight">
            <p className="card-label">Study focus</p>
            <h3>Build understanding before speed</h3>
            <p>
              Ask for explanations, short examples, revision questions, or a step-by-step study plan.
            </p>
          </article>

          <article className="education-panel">
            <p className="card-label">Quick starts</p>
            <div className="topic-chip-grid">
              {studyTopics.map((topic) => (
                <button key={topic} type="button" className="topic-chip" onClick={() => onSendMessage(topic)}>
                  {topic}
                </button>
              ))}
            </div>
          </article>

          <article className="education-panel">
            <p className="card-label">Active sessions</p>
            <div className="session-list-vertical">
              {chatSessions.map((session) => (
                <button
                  key={session.sessionId}
                  type="button"
                  className={activeSession?.sessionId === session.sessionId ? "session-row active" : "session-row"}
                  onClick={() => onSelectSession(session.sessionId)}
                >
                  <strong>{session.sessionTitle}</strong>
                  <span>{session.lastMessagePreview}</span>
                </button>
              ))}
            </div>
          </article>
        </section>

        <section className="assistant-card education-assistant">
          <div className="assistant-header">
            <div>
              <p className="card-label">Learning assistant</p>
              <h3>{activeSession?.sessionTitle ?? "Start a study conversation"}</h3>
            </div>
            <div className="assistant-meta">
              <span>{activeChatbot?.domainName ?? "education"}</span>
              <strong>{chatSessions.length} sessions</strong>
            </div>
          </div>

          <div className="assistant-thread">
            {isChatLoading ? <div className="thread-placeholder">Loading study conversation...</div> : null}
            {!isChatLoading && !(activeSession?.messages?.length) ? (
              <div className="thread-placeholder">Start with a topic, chapter, or concept you want to understand.</div>
            ) : null}
            {activeSession?.messages?.map((message) => (
              <article
                key={message.messageId}
                className={message.senderRole === "USER" ? "chat-bubble user" : "chat-bubble assistant"}
              >
                <div className="chat-bubble-meta">
                  <strong>{message.senderName || message.senderRole}</strong>
                  <span>{formatChatTime(message.createdAt)}</span>
                </div>
                <p>{message.messageContent}</p>
              </article>
            ))}
          </div>

          <form
            className="assistant-composer"
            onSubmit={(event) => {
              event.preventDefault();
              onSendMessage();
            }}
          >
            <textarea
              rows="4"
              value={chatInput}
              onChange={(event) => onChatInputChange(event.target.value)}
              placeholder="Ask for an explanation, revision plan, worked example, or quiz."
            />
            <div className="assistant-actions">
              <div className="quick-actions">
                {suggestedActions.slice(0, 3).map((action) => (
                  <button key={action} type="button" className="quick-action" onClick={() => onSendMessage(action)}>
                    {action}
                  </button>
                ))}
              </div>
              <button type="submit" className="search-submit" disabled={isSendingMessage}>
                {isSendingMessage ? "Sending..." : "Send"}
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  );
}
