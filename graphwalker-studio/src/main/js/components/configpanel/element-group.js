import React, { Component } from 'react';
import { connect } from "react-redux";
import { FormGroup, InputGroup, Switch, TextArea } from "@blueprintjs/core";
import { updateElement, setStartElement } from "../../redux/actions";

class ElementGroup extends Component {
  render() {
    const { id, name, sharedState, guard, actions, requirements, updateElement, isStartElement, setStartElement, disabled } = this.props;
    return (
      <>
        <FormGroup label="Element Name" disabled={disabled}>
          <InputGroup disabled={disabled} value={name} onChange={({ target: { value }}) => updateElement('name', value)}/>
        </FormGroup>
        <FormGroup label="Shared Name" disabled={disabled}>
          <InputGroup disabled={disabled} value={sharedState} onChange={({ target: { value }}) => updateElement('sharedState', value)}/>
        </FormGroup>
        <FormGroup label="Guard" disabled={disabled}>
          <InputGroup disabled={disabled} value={guard} onChange={({ target: { value }}) => updateElement('guard', value)}/>
        </FormGroup>
        <FormGroup label="Actions" disabled={disabled}>
          <div className="bp3-input-group">
            <TextArea disabled={disabled} value={actions} onChange={({ target: { value }}) => updateElement('actions', value)}/>
          </div>
        </FormGroup>
        <FormGroup label="Requirements" disabled={disabled}>
          <div className="bp3-input-group">
            <TextArea disabled={disabled} value={requirements} onChange={({ target: { value }}) => updateElement('requirements', value)}/>
          </div>
        </FormGroup>
        <Switch disabled={disabled} label="Start element" checked={isStartElement} onChange={({ target: { checked }}) => setStartElement(id)}/>
      </>
    )
  }
}

const mapStateToProps = ({ test: { models, selectedModelIndex, selectedElementId }}) => {
  const model = models[selectedModelIndex];
  const elements = [...model.vertices, ...model.edges];
  const element = elements.filter(element => element.id === selectedElementId)[0] || {};
  const { id = "", name = "", sharedState = "", guard = "", actions = [], requirements = [] } = element;
  return {
    id,
    name,
    sharedState,
    guard,
    actions,
    requirements,
    isStartElement: model.startElementId === selectedElementId,
    disabled: selectedElementId === null
  }
};

export default connect(mapStateToProps, { updateElement, setStartElement })(ElementGroup);
