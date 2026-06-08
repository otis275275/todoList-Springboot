import React, { useState } from 'react';
import TodoService from '../services/todo.service';

const AddTodo = ({ loadTodos }) => {
  const [newTodo, setNewTodo] = useState('');

  const handleCreate = async () => {
    if (newTodo.trim()) {
      try {
        await TodoService.createTodo(newTodo);
        setNewTodo('');
        loadTodos();
      } catch (error) {
        console.log(error);
      }
    }
  };

  return (
    <div className="flex mb-4">
      <input
        type="text"
        className="flex-grow px-3 py-2 border rounded-l-lg dark:bg-gray-700 dark:border-gray-600"
        placeholder="Add a new todo"
        value={newTodo}
        onChange={(e) => setNewTodo(e.target.value)}
      />
      <button
        className="bg-blue-600 text-white px-4 py-2 rounded-r-lg hover:bg-blue-700"
        onClick={handleCreate}
      >
        Add
      </button>
    </div>
  );
};

export default AddTodo;