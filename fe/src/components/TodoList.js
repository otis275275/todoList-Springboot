import React from 'react';
import TodoItem from './TodoItem';
import AddTodo from './AddTodo';

const TodoList = ({ todos, loadTodos }) => {
  const highPriorityTodos = todos.filter(todo => !todo.completed).slice(0, 3);
  const upcomingTodos = todos.filter(todo => !todo.completed).slice(3);

  return (
    <div>
      <section className="mb-xl">
        <h3 className="font-label-caps text-label-caps text-outline uppercase mb-md tracking-wider">Today</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-md">
          {highPriorityTodos.map((todo, index) => (
            <TodoItem key={todo.id} todo={todo} loadTodos={loadTodos} isTodaysTask={true} />
          ))}
        </div>
      </section>
      
      <section className="mb-xl">
        <h3 className="font-label-caps text-label-caps text-outline uppercase mb-md tracking-wider">Upcoming</h3>
        <div className="flex flex-col gap-sm">
          {upcomingTodos.map((todo) => (
            <TodoItem key={todo.id} todo={todo} loadTodos={loadTodos} isTodaysTask={false} />
          ))}
        </div>
      </section>
      <AddTodo loadTodos={loadTodos} />
    </div>
  );
};

export default TodoList;
