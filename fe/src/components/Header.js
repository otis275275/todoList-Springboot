import React from 'react';

const Header = () => {
  return (
    <header className="w-full top-0 sticky bg-background dark:bg-background z-40 transition-shadow" id="main-header">
      <div className="flex items-center justify-between px-margin-mobile h-16 w-full max-w-[768px] mx-auto">
        <button className="text-on-surface-variant dark:text-outline hover:bg-surface-container-low transition-colors p-2 rounded-full active:scale-95 duration-150 flex items-center justify-center">
          <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 0" }}>menu</span>
        </button>
        <h1 className="font-headline-md text-headline-md font-bold text-primary dark:text-primary-fixed-dim">Focus</h1>
        <div className="flex items-center">
            <button className="text-on-surface-variant dark:text-outline hover:bg-surface-container-low transition-colors p-2 rounded-full active:scale-95 duration-150 flex items-center justify-center mr-2">
            <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 0" }}>dark_mode</span>
            </button>
            <button className="w-8 h-8 rounded-full overflow-hidden hover:opacity-80 transition-opacity">
            <img alt="User profile avatar" className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBGjirI-MyXJEKFKwEiUyrTScpU6-6FgVbpRIbo4TVI99rgU7qLpTXj73VkJjZxxGvcFSMT5QJOxDXeBNIJjyJDVb-HYjKZUA-cCk8R_dnb9g4BNg1dRIZ-iyZ23uZ3CsYq5w4m0PpBxunMEfl4Bzw8LEh141lD1rX8pR1AxtNjIMhr-Bw_qYrjCX3YM1lXm1uHfmndbgBLALc2I1QzT17or__7l_tPslaqCU-7pnzNbqAGmq3GXg7o3cbxcxv8nScqW4YhNmKC9yY" />
            </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
