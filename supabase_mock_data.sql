-- Mock Data for Connect App

-- IMPORTANT: auth.uid() often returns NULL in the SQL Editor.
-- 1. Sign up/Sign in to your app at least once.
-- 2. Go to your Supabase Dashboard -> Authentication -> Users.
-- 3. Copy your User ID (UUID).
-- 4. Paste it below replacing 'PASTE_YOUR_USER_ID_HERE'.

DO $$
DECLARE
    -- REPLACE THE STRING BELOW WITH YOUR ACTUAL USER ID
    my_user_id uuid := 'PASTE_YOUR_USER_ID_HERE'::uuid;
BEGIN
    -- If you forgot to paste your ID, this will attempt to find the first user in the system
    IF my_user_id IS NULL OR my_user_id = '00000000-0000-0000-0000-000000000000'::uuid THEN
        SELECT id INTO my_user_id FROM auth.users LIMIT 1;
    END IF;

    IF my_user_id IS NULL THEN
        RAISE EXCEPTION 'No user found. Please sign up in the app first or manually provide your User ID UUID in the script.';
    END IF;

    -- Clean up existing mock data for this user to avoid conflicts
    DELETE FROM public.chat_messages WHERE user_id = my_user_id;
    DELETE FROM public.chats WHERE user_id = my_user_id;
    DELETE FROM public.meeting_summaries WHERE user_id = my_user_id;
    DELETE FROM public.timeline_events WHERE user_id = my_user_id;
    DELETE FROM public.contacts WHERE user_id = my_user_id;
    DELETE FROM public.companies WHERE user_id = my_user_id;
    DELETE FROM public.briefing_items WHERE user_id = my_user_id;

    -- 1. Companies
    INSERT INTO public.companies (id, user_id, name, descriptor, funding, employees, open_deals, contacts_inside, recent_news, color)
    VALUES
    ('11111111-1111-1111-1111-111111111111', my_user_id, 'ABC Robotics', 'Robotics · Series B', '$48M', 210, 2, 'Karan Shah, Divya Nair +2 more', 'Closed Series B led by Northwind Capital.', 'NEUTRAL'),
    ('22222222-2222-2222-2222-222222222222', my_user_id, 'Nimbus Robotics', 'Healthcare AI · Series A', '$22M', 85, 1, 'Aditi Rao +1 more', 'Piloting enterprise rollout for Q3.', 'ACCENT'),
    ('33333333-3333-3333-3333-333333333333', my_user_id, 'Northwind Capital', 'Venture capital · fintech focus', '$1.2B AUM', 40, 0, 'Priya Menon', 'Announced a new $200M fund.', 'SUCCESS');

    -- 2. Contacts
    INSERT INTO public.contacts (id, user_id, initials, name, title, company_id, company_name, email, phone, linkedin, score, days_since_contact, shared_interests, mutuals_count, next_action, color)
    VALUES
    ('10000000-0000-0000-0000-000000000001', my_user_id, 'AR', 'Aditi Rao', 'VP Partnerships', '22222222-2222-2222-2222-222222222222', 'Nimbus Robotics', 'aditi.rao@nimbusrobotics.ai', '+91 98450 11223', 'linkedin.com/in/aditirao', 82, 3, ARRAY['healthcare AI'], 3, 'Send the updated proposal before Friday board update.', 'ACCENT'),
    ('20000000-0000-0000-0000-000000000002', my_user_id, 'KS', 'Karan Shah', 'Investor', '11111111-1111-1111-1111-111111111111', 'ABC Robotics', 'karan.shah@abcrobotics.com', '+91 98200 33445', 'linkedin.com/in/karanshah', 78, 1, ARRAY['robotics', 'Series B'], 5, 'Loop in the solutions engineer before the next demo.', 'NEUTRAL'),
    ('30000000-0000-0000-0000-000000000003', my_user_id, 'PM', 'Priya Menon', 'Investor', '33333333-3333-3333-3333-333333333333', 'Northwind Capital', 'priya.menon@northwindcap.com', '+91 90000 12121', 'linkedin.com/in/priyamenon', 64, 12, ARRAY['fintech'], 2, 'Send festival wishes follow-up with a meeting ask.', 'SUCCESS');

    -- 3. Timeline Events
    INSERT INTO public.timeline_events (contact_id, user_id, content, icon, color, is_meeting)
    VALUES
    ('10000000-0000-0000-0000-000000000001', my_user_id, 'Met at AWS Summit', 'LOCATION', 'ACCENT', FALSE),
    ('10000000-0000-0000-0000-000000000001', my_user_id, 'Scanned QR', 'QR', 'NEUTRAL', FALSE),
    ('20000000-0000-0000-0000-000000000002', my_user_id, 'Coffee meeting', 'COFFEE', 'SUCCESS', TRUE),
    ('30000000-0000-0000-0000-000000000003', my_user_id, 'Proposal sent', 'SEND', 'ACCENT', FALSE);

    -- 4. Meeting Summaries
    INSERT INTO public.meeting_summaries (contact_id, user_id, summary, pain_point, budget, action_items)
    VALUES
    ('20000000-0000-0000-0000-000000000002', my_user_id, 'Discussed rollout timeline for the Q3 pilot and pricing for the enterprise tier.', 'onboarding time', '$40-60k', 'Send proposal · Loop in solutions engineer');

    -- 5. Briefing Items
    INSERT INTO public.briefing_items (user_id, text, color)
    VALUES
    (my_user_id, '5 important follow-ups today', 'DANGER'),
    (my_user_id, 'Rahul''s birthday tomorrow', 'ACCENT'),
    (my_user_id, '3 investors viewed your profile', 'PRO'),
    (my_user_id, 'ABC Robotics raised funding', 'SUCCESS');

    -- 6. Chats
    INSERT INTO public.chats (id, user_id, contact_id, last_message, unread_count)
    VALUES
    ('c1111111-1111-1111-1111-111111111111', my_user_id, '10000000-0000-0000-0000-000000000001', 'Proposal follow-up sent', 0),
    ('c2222222-2222-2222-2222-222222222222', my_user_id, '20000000-0000-0000-0000-000000000002', 'Demo completed, contract next', 2),
    ('c3333333-3333-3333-3333-333333333333', my_user_id, '30000000-0000-0000-0000-000000000003', 'Festival wishes exchanged', 0);

    -- 7. Chat Messages
    INSERT INTO public.chat_messages (chat_id, user_id, content, from_user, is_ai_labeled)
    VALUES
    ('c1111111-1111-1111-1111-111111111111', my_user_id, 'Who should I follow up with today?', TRUE, FALSE),
    ('c1111111-1111-1111-1111-111111111111', my_user_id, 'Aditi Rao (proposal pending) and 4 others need a reply this week.', FALSE, TRUE),
    ('c1111111-1111-1111-1111-111111111111', my_user_id, 'Draft a follow-up to Aditi', TRUE, FALSE);
END $$;
