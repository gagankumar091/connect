-- Tables for Connect App

-- Profiles table (linked to auth.users)
CREATE TABLE public.profiles (
  id UUID REFERENCES auth.users NOT NULL PRIMARY KEY,
  name TEXT,
  email TEXT,
  initials TEXT,
  title TEXT,
  company TEXT,
  phone TEXT,
  linkedin TEXT,
  avatar_url TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Companies table
CREATE TABLE public.companies (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users NOT NULL,
  name TEXT NOT NULL,
  descriptor TEXT,
  funding TEXT,
  employees INTEGER,
  open_deals INTEGER DEFAULT 0,
  contacts_inside TEXT,
  recent_news TEXT,
  color TEXT, -- e.g. "ACCENT", "PRO", "SUCCESS", "DANGER", "NEUTRAL"
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.companies ENABLE ROW LEVEL SECURITY;

-- Contacts table
CREATE TABLE public.contacts (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users NOT NULL,
  initials TEXT,
  name TEXT NOT NULL,
  title TEXT,
  company_id UUID REFERENCES public.companies(id),
  company_name TEXT, -- Fallback if company not in companies table
  email TEXT,
  phone TEXT,
  linkedin TEXT,
  score INTEGER DEFAULT 0,
  days_since_contact INTEGER DEFAULT 0,
  shared_interests TEXT[],
  mutuals_count INTEGER DEFAULT 0,
  next_action TEXT,
  color TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.contacts ENABLE ROW LEVEL SECURITY;

-- Timeline events
CREATE TABLE public.timeline_events (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  contact_id UUID REFERENCES public.contacts(id) ON DELETE CASCADE,
  user_id UUID REFERENCES auth.users NOT NULL,
  content TEXT NOT NULL,
  icon TEXT, -- e.g. "LOCATION", "QR", "FILE", "COFFEE", "SEND", "CHECK"
  color TEXT,
  is_meeting BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.timeline_events ENABLE ROW LEVEL SECURITY;

-- Meeting summaries
CREATE TABLE public.meeting_summaries (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  contact_id UUID REFERENCES public.contacts(id) ON DELETE CASCADE,
  user_id UUID REFERENCES auth.users NOT NULL,
  summary TEXT,
  pain_point TEXT,
  budget TEXT,
  action_items TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.meeting_summaries ENABLE ROW LEVEL SECURITY;

-- Briefing items
CREATE TABLE public.briefing_items (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users NOT NULL,
  text TEXT NOT NULL,
  color TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.briefing_items ENABLE ROW LEVEL SECURITY;

-- Chats (conversations)
CREATE TABLE public.chats (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES auth.users NOT NULL,
  contact_id UUID REFERENCES public.contacts(id),
  last_message TEXT,
  unread_count INTEGER DEFAULT 0,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.chats ENABLE ROW LEVEL SECURITY;

-- Chat messages
CREATE TABLE public.chat_messages (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  chat_id UUID REFERENCES public.chats(id) ON DELETE CASCADE,
  user_id UUID REFERENCES auth.users NOT NULL,
  content TEXT NOT NULL,
  from_user BOOLEAN DEFAULT TRUE,
  is_ai_labeled BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;

-- Connections table for requests and communication status
CREATE TABLE public.connections (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  sender_id UUID REFERENCES auth.users NOT NULL,
  receiver_contact_id UUID REFERENCES public.contacts(id) NOT NULL,
  status TEXT DEFAULT 'PENDING', -- PENDING, ACCEPTED, DECLINED
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
  UNIQUE(sender_id, receiver_contact_id)
);

ALTER TABLE public.connections ENABLE ROW LEVEL SECURITY;

-- Policies for connections
CREATE POLICY "Users can manage their own sent connections" ON public.connections FOR ALL USING (auth.uid() = sender_id);
CREATE POLICY "Users can see connections sent to them" ON public.connections FOR SELECT USING (
  EXISTS (
    SELECT 1 FROM public.contacts c
    WHERE c.id = receiver_contact_id AND c.user_id = auth.uid()
  )
);

-- Policies for RLS (simplified: user can only see their own data)
CREATE POLICY "Users can view their own profiles" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can insert their own profiles" ON public.profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Users can update their own profiles" ON public.profiles FOR UPDATE USING (auth.uid() = id);

CREATE POLICY "Users can manage their own companies" ON public.companies FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users can manage their own contacts" ON public.contacts FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users can manage their own timeline_events" ON public.timeline_events FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users can manage their own meeting_summaries" ON public.meeting_summaries FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users can manage their own briefing_items" ON public.briefing_items FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users can manage their own chats" ON public.chats FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Users can manage their own chat_messages" ON public.chat_messages FOR ALL USING (auth.uid() = user_id);
