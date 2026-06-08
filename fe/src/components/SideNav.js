import React from 'react';

const SideNav = () => {
  return (
    <div className="hidden md:flex fixed top-16 left-0 h-[calc(100vh-64px)] w-64 flex-col bg-surface border-r border-surface-container-highest p-4 gap-2">
      <h3 className="font-label-caps text-label-caps text-outline uppercase mb-2 px-4 tracking-wider">Navigation</h3>
      <button className="flex items-center gap-3 px-4 py-3 bg-secondary-container text-on-secondary-container font-headline-sm text-headline-sm rounded-lg w-full text-left">
        <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>check_circle</span> Tasks
      </button>
      <button className="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container-high rounded-lg w-full text-left font-body-lg text-body-lg transition-colors">
        <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 0" }}>calendar_today</span> Calendar
      </button>
      <button className="flex items-center gap-3 px-4 py-3 text-on-surface-variant hover:bg-surface-container-high rounded-lg w-full text-left font-body-lg text-body-lg transition-colors mt-auto">
        <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 0" }}>settings</span> Settings
      </button>
    </div>
  );
};

export default SideNav;
