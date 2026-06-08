import React from 'react';

const BottomNav = () => {
  return (
    <nav className="fixed bottom-0 w-full z-50 rounded-t-xl md:hidden bg-surface-container-lowest dark:bg-surface-container-lowest shadow-[0_-2px_8px_rgba(0,0,0,0.05)] border-t-0">
      <div className="fixed bottom-0 left-0 w-full flex justify-around items-center py-2 px-margin-mobile bg-surface-container-lowest">
        <button className="flex flex-col items-center justify-center bg-primary-container dark:bg-primary-container text-on-primary-container rounded-full px-4 py-1 hover:opacity-80 active:scale-90 transition-transform">
          <span className="material-symbols-outlined mb-1" style={{ fontVariationSettings: "'FILL' 1" }}>check_circle</span>
          <span className="font-label-caps text-label-caps">Tasks</span>
        </button>
        <button className="flex flex-col items-center justify-center text-on-surface-variant dark:text-outline px-4 py-1 hover:opacity-80">
          <span className="material-symbols-outlined mb-1" style={{ fontVariationSettings: "'FILL' 0" }}>calendar_today</span>
          <span className="font-label-caps text-label-caps">Calendar</span>
        </button>
        <button className="flex flex-col items-center justify-center text-on-surface-variant dark:text-outline px-4 py-1 hover:opacity-80">
          <span className="material-symbols-outlined mb-1" style={{ fontVariationSettings: "'FILL' 0" }}>settings</span>
          <span className="font-label-caps text-label-caps">Settings</span>
        </button>
      </div>
    </nav>
  );
};

export default BottomNav;
