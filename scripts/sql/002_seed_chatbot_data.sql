INSERT INTO chatbot_definition (
    chatbot_code,
    display_name,
    domain_name,
    description,
    system_prompt,
    default_model,
    temperature,
    max_response_tokens,
    welcome_message,
    configuration
)
VALUES
(
    'travel_ai_guide',
    'TravelAiGuide',
    'travel',
    'Travel planning assistant for destinations, itineraries, hotel suggestions, and booking guidance.',
    'You are TravelAiGuide. Help users plan trips, compare destinations, suggest itineraries, explain travel logistics, and keep answers practical and concise.',
    'gpt-4o-mini',
    0.45,
    1200,
    'Hi, I can help you plan destinations, itineraries, and travel bookings.',
    '{"supportsItineraryGeneration": true, "supportsBudgetPlanning": true, "supportsBookingHelp": true}'::jsonb
),
(
    'education_helpful',
    'EducationHelpful',
    'education',
    'Learning assistant for study plans, concept explanations, quizzes, and revision support.',
    'You are EducationHelpful. Explain concepts clearly, adapt to the learner level, break down complex topics, and offer structured study support.',
    'gpt-4o-mini',
    0.35,
    1100,
    'Hi, I can help with study plans, explanations, and revision questions.',
    '{"supportsStudyPlans": true, "supportsQuizGeneration": true, "supportsLessonSummaries": true}'::jsonb
)
ON CONFLICT (chatbot_code) DO UPDATE
SET display_name = EXCLUDED.display_name,
    domain_name = EXCLUDED.domain_name,
    description = EXCLUDED.description,
    system_prompt = EXCLUDED.system_prompt,
    default_model = EXCLUDED.default_model,
    temperature = EXCLUDED.temperature,
    max_response_tokens = EXCLUDED.max_response_tokens,
    welcome_message = EXCLUDED.welcome_message,
    configuration = EXCLUDED.configuration,
    is_active = TRUE,
    updated_at = NOW();

INSERT INTO chatbot_capability (chatbot_id, capability_name, capability_description)
SELECT chatbot_id, capability_name, capability_description
FROM (
    SELECT
        cd.chatbot_id,
        'destination_recommendation' AS capability_name,
        'Suggest destinations based on season, budget, and interests.' AS capability_description
    FROM chatbot_definition cd
    WHERE cd.chatbot_code = 'travel_ai_guide'

    UNION ALL

    SELECT
        cd.chatbot_id,
        'itinerary_generation',
        'Create daily travel plans with transport and activity suggestions.'
    FROM chatbot_definition cd
    WHERE cd.chatbot_code = 'travel_ai_guide'

    UNION ALL

    SELECT
        cd.chatbot_id,
        'booking_guidance',
        'Answer booking, cancellation, and travel preparation questions.'
    FROM chatbot_definition cd
    WHERE cd.chatbot_code = 'travel_ai_guide'

    UNION ALL

    SELECT
        cd.chatbot_id,
        'concept_explanation',
        'Explain educational topics in simple and structured language.'
    FROM chatbot_definition cd
    WHERE cd.chatbot_code = 'education_helpful'

    UNION ALL

    SELECT
        cd.chatbot_id,
        'study_plan_creation',
        'Build study schedules based on learner goals and time constraints.'
    FROM chatbot_definition cd
    WHERE cd.chatbot_code = 'education_helpful'

    UNION ALL

    SELECT
        cd.chatbot_id,
        'quiz_support',
        'Generate practice questions and revision prompts.'
    FROM chatbot_definition cd
    WHERE cd.chatbot_code = 'education_helpful'
) seeded_capabilities
ON CONFLICT (chatbot_id, capability_name) DO NOTHING;
