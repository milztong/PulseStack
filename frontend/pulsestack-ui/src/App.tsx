import { useState, useEffect, lazy, Suspense } from 'react';
import { Sidebar } from './components/Sidebar';
import { ChannelFeed } from './components/ChannelFeed';
import { LoginPage } from './components/LoginPage';
import { ErrorBoundary } from './components/ErrorBoundary';
import { ChatPanel } from './components/ChatPanel';
import { useAuth } from './hooks/useAuth';

const AnalyticsDashboard = lazy(() =>
  import('./components/AnalyticsDashboard').then(m => ({ default: m.AnalyticsDashboard }))
);

const PredictorView = lazy(() =>
  import('./components/PredictorView').then(m => ({ default: m.PredictorView }))
);

const API_URL = `${import.meta.env.VITE_INGESTION_URL ?? 'http://localhost:8081'}/api/v1/channels`;

interface ChannelData {
  id: string;
  name: string;
  displayName: string;
  description?: string;
}

type View = 'feed' | 'analytics' | 'predictor';

export default function App() {
  const { auth, login, register, logout } = useAuth();
  const [channels, setChannels] = useState<ChannelData[]>([]);
  const [activeChannelName, setActiveChannelName] = useState<string>('java');
  const [view, setView] = useState<View>('feed');

  useEffect(() => {
    fetch(API_URL)
      .then(res => res.json())
      .then(setChannels)
      .catch(() => console.warn('Could not load channels from API'));
  }, []);

  if (!auth.token) {
    return <LoginPage onLogin={login} onRegister={register} />;
  }

  const activeChannel = channels.find(c => c.name === activeChannelName);

  return (
    <div className="flex flex-col h-screen bg-gray-950 text-white overflow-hidden">
      {/* Top nav */}
      <header className="flex items-center justify-between px-4 py-2 bg-gray-900 border-b border-gray-800 shrink-0">
        <div className="flex items-center gap-1">
          <span className="font-bold text-lg text-indigo-400 mr-4">PulseStack</span>
          <button
            onClick={() => setView('feed')}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              view === 'feed' ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:text-white'
            }`}
          >
            Feed
          </button>
          <button
            onClick={() => setView('analytics')}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              view === 'analytics' ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:text-white'
            }`}
          >
            Analytics
          </button>
          <button
            onClick={() => setView('predictor')}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              view === 'predictor' ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:text-white'
            }`}
          >
            Predictor
          </button>
        </div>
        <div className="flex items-center gap-3 text-sm">
          <span className="text-gray-400">
            Signed in as <span className="text-white font-medium">{auth.username}</span>
          </span>
          <button
            onClick={logout}
            className="text-gray-500 hover:text-red-400 transition-colors"
          >
            Sign out
          </button>
        </div>
      </header>

      {/* Body */}
      <div className="flex flex-1 overflow-hidden">
        {view === 'feed' && (
          <>
            <Sidebar
              activeChannelName={activeChannelName}
              onChannelSelect={setActiveChannelName}
            />
            <ErrorBoundary>
              <ChannelFeed
                channelId={activeChannel?.id ?? null}
                channelName={activeChannelName}
              />
            </ErrorBoundary>
            <ErrorBoundary>
              <ChatPanel
                channelId={activeChannel?.id ?? null}
                username={auth.username}
              />
            </ErrorBoundary>
          </>
        )}
        {view === 'analytics' && (
          <ErrorBoundary>
            <Suspense fallback={<div className="flex-1 flex items-center justify-center text-gray-400">Loading analytics…</div>}>
              <AnalyticsDashboard />
            </Suspense>
          </ErrorBoundary>
        )}
        {view === 'predictor' && (
          <ErrorBoundary>
            <Suspense fallback={<div className="flex-1 flex items-center justify-center text-gray-400">Loading predictor…</div>}>
              <PredictorView token={auth.token} username={auth.username} />
            </Suspense>
          </ErrorBoundary>
        )}
      </div>
    </div>
  );
}
