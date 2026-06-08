import React, { useState, useEffect } from 'react';
import TodoService from '../services/todo.service';

const TodoItem = ({ todo, loadTodos, isTodaysTask }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [newTitle, setNewTitle] = useState(todo.title);

  useEffect(() => {
    const checkbox = document.getElementById(`checkbox-${todo.id}`);
    if (checkbox) {
        checkbox.addEventListener('change', (e) => {
            const taskCard = e.target.closest('.task-item');
            if (e.target.checked) {
                taskCard.classList.add('completed');
            } else {
                taskCard.classList.remove('completed');
            }
        });
    }
  }, [todo.id]);

  const handleToggle = async () => {
    try {
      await TodoService.updateTodo(todo.id, { ...todo, completed: !todo.completed });
      loadTodos();
    } catch (error) {
      console.log(error);
    }
  };

  const handleDelete = async () => {
    try {
      await TodoService.deleteTodo(todo.id);
      loadTodos();
    } catch (error) {
      console.log(error);
    }
  };

  const handleEdit = async () => {
    if (isEditing) {
      try {
        await TodoService.updateTodo(todo.id, { ...todo, title: newTitle });
        loadTodos();
      } catch (error) {
        console.log(error);
      }
    }
    setIsEditing(!isEditing);
  };

  if (isTodaysTask) {
    return (
        <div className="task-card task-item bg-surface-container-lowest border border-outline-variant/30 rounded-xl p-md flex items-start gap-md shadow-sm">
            <input id={`checkbox-${todo.id}`} className="custom-checkbox mt-xs flex-shrink-0" type="checkbox" checked={todo.completed} onChange={handleToggle} />
            <div className="flex-col flex-grow">
                <div className="flex items-center justify-between gap-md mb-xs">
                    {isEditing ? (
                        <input
                        type="text"
                        className="flex-grow px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                        value={newTitle}
                        onChange={(e) => setNewTitle(e.target.value)}
                        />
                    ) : (
                        <span className={`task-title font-body-lg text-body-lg text-on-surface block ${todo.completed ? 'completed' : ''}`}>{todo.title}</span>
                    )}
                    <span className="font-body-md text-body-md text-on-surface-variant flex items-center gap-1 whitespace-nowrap">
                        <span className="material-symbols-outlined text-[16px]">schedule</span> 10:00 AM
                    </span>
                </div>
                <div className="flex items-center justify-between">
                    <span className="font-label-caps text-label-caps px-2 py-1 bg-error-container text-on-error-container rounded-md">High Priority</span>
                    <div>
                        <button className="text-on-surface-variant hover:bg-surface-container-low transition-colors p-1 rounded-full" onClick={handleEdit}>
                            <span className="material-symbols-outlined text-[20px]">{isEditing ? 'save' : 'edit'}</span>
                        </button>
                        <button className="text-red-500 hover:text-red-700" onClick={handleDelete}>
                            <span className="material-symbols-outlined text-[20px]">delete</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
  }

  return (
    <div className="task-card task-item bg-surface-container-lowest border border-surface-container-highest rounded-lg p-md flex items-center gap-md">
        <input id={`checkbox-${todo.id}`} className="custom-checkbox flex-shrink-0" type="checkbox" checked={todo.completed} onChange={handleToggle}/>
        {isEditing ? (
            <input
            type="text"
            className="flex-grow px-3 py-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            />
        ) : (
            <span className={`task-title font-body-md text-body-md text-on-surface flex-grow ${todo.completed ? 'completed' : ''}`}>{todo.title}</span>
        )}
        <span className="font-body-md text-body-md text-on-surface-variant flex-shrink-0">Tomorrow</span>
        <button className="text-on-surface-variant hover:bg-surface-container-low transition-colors p-1 rounded-full" onClick={handleEdit}>
            <span className="material-symbols-outlined text-[20px]">{isEditing ? 'save' : 'edit'}</span>
        </button>
        <button className="text-red-500 hover:text-red-700" onClick={handleDelete}>
            <span className="material-symbols-outlined text-[20px]">delete</span>
        </button>
    </div>
  );
};

export default TodoItem;