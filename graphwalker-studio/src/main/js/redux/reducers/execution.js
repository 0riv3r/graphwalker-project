import {
  EXECUTION_CONNECT,
  EXECUTION_DELAY,
  EXECUTION_FAILED,
  EXECUTION_FULFILLED,
  EXECUTION_LOAD,
  EXECUTION_PAUSE,
  EXECUTION_RUN,
  EXECUTION_STEP,
  EXECUTION_STOP
} from "../actionTypes";

const initialState = {
  running: false,
  paused: false,
  delay: 0,
  stopConditionFulfillment: 0,
  totalCount: 0,
  visited: {}
}

export default function(state = initialState, action) {
  switch (action.type) {
    case EXECUTION_CONNECT: {
      console.log('EXECUTION_CONNECT', action.payload);
      return {
        ...state,
        running: false,
        paused: false
      }
    }
    case EXECUTION_DELAY: {
      const { value } = action.payload;
      return {
        ...state,
        delay: value
      }
    }
    case EXECUTION_FAILED: {
      console.log('EXECUTION_FAILED', action.payload);
      return {
        ...state,
        running: false,
        paused: false
      }
    }
    case EXECUTION_FULFILLED: {
      console.log('EXECUTION_FULFILLED', action.payload);
      return {
        ...state,
        running: false,
        paused: false
      }
    }
    case EXECUTION_LOAD: {
      console.log('EXECUTION_LOAD', action.payload);
      return {
        ...state,
        stopConditionFulfillment: 0,
        totalCount: 0,
        visited: {}
      }
    }
    case EXECUTION_PAUSE: {
      console.log('EXECUTION_PAUSE', action.payload);
      return {
        ...state,
        running: false,
        paused: true
      }
    }
    case EXECUTION_RUN: {
      console.log('EXECUTION_RUN', action.payload);
      return {
        ...state,
        running: true,
        paused: false
      }
    }
    case EXECUTION_STEP: {
      console.log('EXECUTION_STEP', action.payload);
      const { command, modelId, elementId, stopConditionFulfillment, visitedCount, totalCount } = action.payload.response;
      if (command === 'visitedElement') {
        return {
          ...state,
          stopConditionFulfillment,
          totalCount,
          visited: {
            ...state.visited,
            [modelId]: {
              ...state.visited[modelId],
              [elementId]: {
                visitedCount
              }
            }
          }
        }
      } else {
        return {
          ...state
        }
      }
    }
    case EXECUTION_STOP: {
      console.log('EXECUTION_STOP', action.payload);
      return {
        ...state,
        running: false,
        paused: false,
        stopConditionFulfillment: 0,
        totalCount: 0,
        visited: {}
      }
    }
    default: {
      return state;
    }
  }
}
